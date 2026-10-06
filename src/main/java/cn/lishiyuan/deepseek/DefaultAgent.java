package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.api.chat.ChatRequest;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.Choice;
import cn.lishiyuan.deepseek.api.chat.Message;
import cn.lishiyuan.deepseek.api.chat.Thinking;
import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.api.chat.msg.AssistantMessage;
import cn.lishiyuan.deepseek.api.chat.msg.ChatRequestMessage;
import cn.lishiyuan.deepseek.api.chat.msg.SystemMessage;
import cn.lishiyuan.deepseek.api.chat.msg.ToolMessage;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.config.AgentConfig;
import cn.lishiyuan.deepseek.config.enums.FinishReasonEnums;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.config.enums.SendTimeEnums;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;

/**
 * 一个简单的agent loop示例实现，实现loop和工具调用功能。注意，不要在生产环境使用此例子。
 */
@Slf4j
public class DefaultAgent implements Agent {

    /** 每次模型请求的日志分类（Reactor log 算子用，Level.FINE 对应 SLF4J 的 debug）。 */
    private static final String REQUEST_LOG = "cn.lishiyuan.deepseek.agent.request";
    /** 每个轮次的日志分类。 */
    private static final String LOOP_LOG = "cn.lishiyuan.deepseek.agent.loop";

    private final Client client;
    private volatile AgentConfig config;
    private final Object SIGNAL = new Object();
    private final Sinks.Many<Object> LOOP = Sinks.many().unicast().onBackpressureBuffer();

    // 非插队消息
    private final BlockingQueue<Send> TURN_MSG = new LinkedBlockingQueue<>();
    // 插队消息
    private final BlockingQueue<Send>  REQUEST_MSG = new LinkedBlockingQueue<>();

    /**
     * 全局会话上下文，是整个上下文。每次turn完成需要对上个turn的上下文进行简化，去除不必要的token
     */
    private final List<ChatRequestMessage> history = new ArrayList<>();

    /**
     * 当前会话
     */
    private final Disposable loop;

    public DefaultAgent(Client client, AgentConfig config) {
        this.client = client;
        this.config = config;
        // 构建turn管道
        this.loop = LOOP.asFlux()
                // single()：turn loop 单线程串行，天然互斥（等价于原 scaffold 的单线程 scheduler）
                .publishOn(Schedulers.single())
                // concatMap：一次只跑一个 turn，turn 之间串行
                .concatMap(signal -> this.loop())
                .subscribe(
                        ignored -> { },
                        e -> log.error("agent loop 终止", e));
    }

    @Override
    public void configChange(AgentConfig config) {
        this.config = config;
        log.debug("agent config 已更新: {}", config.getId());
    }

    @Override
    public Mono<ChatResponse> send(UserMessage userMessage, SendTimeEnums sendTime) {
        if (loop.isDisposed()){
            throw new DeepSeekException("loop is disposed");
        }
        SendTimeEnums time = sendTime != null ? sendTime : config.getDefaultSendTime();
        Send send = new Send(userMessage, time, Sinks.one());
        if (time == SendTimeEnums.NEXT_REQUEST) {
            // 插队：交给当前 turn 内部消费
            addQueue(REQUEST_MSG, send);
        } else {
            // 排队：等下一个 turn 消费
            addQueue(TURN_MSG, send);
        }

        return send.response().asMono();
    }

    private void addQueue(Queue<Send> queue, Send send){
        queue.add(send);
        // 并发投递时 tryEmitNext 可能返回 FAIL_NON_SERIALIZED：必须重试，否则驱动信号丢失，
        // 队列里的消息要等到下一次信号才被消费（若这是最后一条，对应的 Mono 会一直挂起）。
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        Sinks.EmitResult result;
        do {
            result = LOOP.tryEmitNext(SIGNAL);
            if (result == Sinks.EmitResult.FAIL_NON_SERIALIZED) {
                LockSupport.parkNanos(1_000L);
            }
        } while (result == Sinks.EmitResult.FAIL_NON_SERIALIZED && System.nanoTime() < deadline);
        if (!result.isSuccess()) {
            log.warn("agent 投递驱动信号失败: {}", result);
        }
    }

    /** 关闭 agent loop；并把剩下的未消费消息全部以错误结束，避免调用方的 Mono 永远挂起。 */
    public void dispose() {
        loop.dispose();
        List<Send> leftovers = new ArrayList<>();
        REQUEST_MSG.drainTo(leftovers);
        TURN_MSG.drainTo(leftovers);
        for (Send send : leftovers) {
            send.response().tryEmitError(new DeepSeekException("agent 已关闭，消息未被处理"));
        }
        log.debug("agent 已关闭，未处理消息 {} 条", leftovers.size());
    }

    // ── loop ────────────────────────────────────────────────────────────────
    private Mono<Void> loop(){

        // 拉取全部消息
        List<Send> requestMsg = new ArrayList<>();
        List<Send> turnMsg = new ArrayList<>();
        REQUEST_MSG.drainTo(requestMsg);
        TURN_MSG.drainTo(turnMsg);

        // 没有消息则跳过，单线程不需要担心消费问题
        if (requestMsg.isEmpty() && turnMsg.isEmpty()) {
            return Mono.empty();
        }

        // 构建上下文
        TurnState turnState = new TurnState();
        turnState.inTurn.addAll(requestMsg.stream().map(Send::message).toList());
        turnState.consumed.addAll(requestMsg);
        turnState.inTurn.addAll(turnMsg.stream().map(Send::message).toList());
        turnState.consumed.addAll(turnMsg);

        // 接下来使用应用，获取response
        // 最后将responses发送出去
        return Mono.just(turnState)
                .expand(this::turn)
                // 注意：expand 会把「种子状态」和每个中间状态都发射出来，
                // 因此只能在最后一个状态上做收尾，否则种子的 lastResponse 还是 null，
                // tryEmitValue(null) 会先把响应槽「空完成」掉，真正的响应就再也发不出去了。
                .last()
                .log(LOOP_LOG, Level.FINE, SignalType.ON_NEXT, SignalType.ON_ERROR)
                .doOnNext(result -> {
                    // 完成本轮所有响应：本轮最终响应
                    ChatResponse lastResponse = result.lastResponse;
                    for (Send s : result.consumed) {
                        s.response.tryEmitValue(lastResponse);
                    }
                    // 本轮上下文并入常驻 history。只做一次，避免 expand 多次发射导致重复累积
                    this.history.addAll(result.inTurn);
                })
                .then();
    }

    private Mono<TurnState> turn(TurnState turn){
        return client.post(buildRequest(turn))
                // 请求失败先记 error 日志（否则错误只在 loop 的 subscribe 里被吞掉）
                .doOnError(e -> log.error("agent request 失败：{}", e.getMessage(), e))
                // 每次请求结束：onNext 打印响应（content / reasoning_content / tool_calls，带 id），
                // onComplete 打印一行结束标记
                .log(REQUEST_LOG, Level.FINE, SignalType.ON_NEXT,  SignalType.ON_ERROR)
                .flatMap(response -> {

                    turn.setLastResponse(response);
                    Choice choice = firstChoice(response);
                    if (choice == null) {
                        return Mono.empty();
                    }
                    // 累积消息
                    Message message = choice.getMessage();
                    if (message != null) {
                        // 并入上下文
                        AssistantMessage assistantMessage = new AssistantMessage();
                        assistantMessage.setContent(message.getContent());
                        assistantMessage.setReasoningContent(message.getReasoningContent());
                        assistantMessage.setToolCalls(message.getToolCalls());
                        turn.getInTurn().add(assistantMessage);
                    }
                    FinishReasonEnums finishReason = FinishReasonEnums.fromCode(choice.getFinishReason());
                    if (finishReason == FinishReasonEnums.TOOL_CALLS) {
                List<ToolCall> toolCalls = message == null
                        ? null : message.getToolCalls();
                if (toolCalls == null || toolCalls.isEmpty()) {
                    // 构建工具列表为空的消息则直接回传
                    // 将新入消息传入
                    List<Send> requestMsg = new ArrayList<>();
                    REQUEST_MSG.drainTo(requestMsg);
                    turn.consumed.addAll(requestMsg);
                    turn.inTurn.addAll(requestMsg.stream().map(Send::message).toList());
                    return Mono.just(turn);
                }

                return Flux.fromIterable(toolCalls)
                        // flatMapSequential：并发执行、结果顺序与 tool_calls 一致
                        .flatMapSequential(toolCall -> Mono.fromCallable(() -> {
                            String result = config.getToolsCaller().call(toolCall);
                            ToolMessage toolMessage = new ToolMessage();
                            toolMessage.setToolCallId(toolCall.getId());
                            toolMessage.setContent(result);
                            return toolMessage;
                        }).subscribeOn(Schedulers.boundedElastic()))
                        .collectList().doOnNext(toolMsg->{
                            turn.inTurn.addAll(toolMsg);
                            // 将新入消息传入
                            List<Send> requestMsg = new ArrayList<>();
                            REQUEST_MSG.drainTo(requestMsg);
                            turn.consumed.addAll(requestMsg);
                            turn.inTurn.addAll(requestMsg.stream().map(Send::message).toList());
                        })
                        .then(Mono.just(turn));
            }

            return Mono.empty();
        });
    }

    private ChatRequest buildRequest(TurnState turn) {
        AgentConfig c = config;
        List<ChatRequestMessage> messages = new ArrayList<>();
        // system 每次请求动态前置，configChange 后立即生效
        if (c.getSystemPrompt() != null && !c.getSystemPrompt().isBlank()) {
            SystemMessage systemMessage = new SystemMessage();
            systemMessage.setContent(c.getSystemPrompt());
            messages.add(systemMessage);
        }
        // 加载历史会话与本轮新增消息
        messages.addAll(this.history);
        messages.addAll(turn.getInTurn());

        ChatRequest request = new ChatRequest();
        request.setMessages(messages);
        request.setModel(c.getModel() != null ? c.getModel().code : ModelEnums.DEEPSEEK_FLASH.code);

        Thinking thinking = new Thinking();
        thinking.setType(c.getThinking() != null ? c.getThinking().code : null);
        thinking.setReasoningEffort(c.getThinkingEffort() != null ? c.getThinkingEffort().code : null);
        request.setThinking(thinking);

        request.setMaxTokens(c.getMaxTokens());
        request.setResponseFormat(c.getResponseFormat());
        request.setStop(c.getStop());
        request.setTemperature(c.getTemperature());
        request.setTopP(c.getTopP());
        request.setToolChoice(c.getToolChoice());
        request.setLogprobs(c.getLogprobs());
        request.setTopLogprobs(c.getTopLogprobs());

        // toolsCaller 可能没配（那就没有工具可用，避免 NPE）
        ToolsCaller toolsCaller = c.getToolsCaller();
        List<Tool> tools = toolsCaller == null ? null : toolsCaller.tools();
        request.setTools(tools);
        return request;
    }

    private static Choice firstChoice(ChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return null;
        }
        return response.getChoices().get(0);
    }

    /** 一次 send 的上下文：消息、响应槽与发送时机。 */
    private record Send(UserMessage message, SendTimeEnums sendTime, Sinks.One<ChatResponse> response) { }


    /** 单个轮次的执行状态（轮次结束即丢弃）。 */
    @Data
    private static class TurnState {
        /** 本轮被消费的所有 send，轮次结束时统一完成 */
        private final List<Send> consumed = new ArrayList<>();
        /** 待随下一次请求发送的消息（本轮种子 / 插队消息） */
        private final List<ChatRequestMessage> inTurn = new ArrayList<>();
        /** 本轮最后一次模型响应 */
        private ChatResponse lastResponse;
    }
}
