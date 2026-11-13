package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.EmptyRequest;
import cn.lishiyuan.deepseek.api.chat.*;
import cn.lishiyuan.deepseek.api.chat.msg.*;
import cn.lishiyuan.deepseek.api.common.SafetyCheck;
import cn.lishiyuan.deepseek.api.message.MessageContentBlock;
import cn.lishiyuan.deepseek.api.message.MessageInput;
import cn.lishiyuan.deepseek.api.message.MessageRequest;
import cn.lishiyuan.deepseek.api.message.MessageResponse;
import cn.lishiyuan.deepseek.api.message.MessageStreamEvent;
import cn.lishiyuan.deepseek.api.message.MessageStreamRequest;
import cn.lishiyuan.deepseek.api.message.MessageTool;
import cn.lishiyuan.deepseek.api.common.StreamOptions;
import cn.lishiyuan.deepseek.api.common.TitleOptions;
import cn.lishiyuan.deepseek.api.common.Usage;
import cn.lishiyuan.deepseek.api.fim.StreamFimRequest;
import cn.lishiyuan.deepseek.api.response.ResponseRequest;
import cn.lishiyuan.deepseek.api.response.ResponseResult;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.enums.*;
import cn.lishiyuan.deepseek.api.fim.FimRequest;
import cn.lishiyuan.deepseek.api.fim.FimResponse;
import cn.lishiyuan.deepseek.api.platform.ListModelResponse;
import cn.lishiyuan.deepseek.api.platform.BalanceInfoResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@DisplayName("Client测试")
public class ClientTests {

    /**
     * 静态初始化客户端（不用 {@code @BeforeAll}：旧版 surefire 不执行 JUnit5 生命周期方法，
     * 会让 client 为 null）。
     */
    private static final Client client = initClient();

    private static Client initClient() {
        Path path = Paths.get(ClientTests.class.getClassLoader().getResource("accessKey.txt").getPath());
        try {
            String accessKey = Files.readString(path);
            return new DefaultClient(accessKey);
        } catch (IOException e) {
            throw new RuntimeException("读取accessKey失败", e);
        }
    }

    @Test
    @DisplayName("测试对话")
    public void testChat(){
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful assistant");

        UserMessage userMessage = new UserMessage();
        userMessage.setContent("请仿照《沁园春·长沙》写一篇词，题为《沁园春·西安》");

        List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);
        Thinking thinking = new Thinking();
        thinking.setType(ThinkingEnums.ENABLED.code);
        chatRequest.setThinking(thinking);
        // 推理强度是请求体顶层字段（已从 thinking 中拆分）
        chatRequest.setReasoningEffort(ThinkingEffortEnums.MAX.code);
        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse,"chatResponse不应该为空");
    }

    @Test
    @DisplayName("测试流对话")
    public void testStreamChat(){
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful assistant");

        UserMessage userMessage = new UserMessage();
        userMessage.setContent("请仿照《沁园春·长沙》写一篇词，题为《沁园春·西安》");

        List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
        StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);

        client.stream(chatRequest)
                .doOnNext(chatResponse -> Assertions.assertNotNull(chatResponse,"chatResponse"))
                .blockLast();
    }

    @Test
    @DisplayName("测试FIM")
    public void testFIM(){
        FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_FLASH.code);
        FimResponse fimResponse = client.post(fimRequest).block();
        Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
    }

    @Test
    @DisplayName("测试流FIM")
    public void testStreamFIM() {
        StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_FLASH.code);
        client.stream(fimRequest)
                .doOnNext(fimResponse -> Assertions.assertNotNull(fimResponse,"fimResponse"))
                .blockFirst();
    }

    @Test
    @DisplayName("测试列出模型")
    public void testListModel(){
        ListModelResponse listModelResponse = client.get(EmptyRequest.createListModelRequest()).block();
        String name = listModelResponse.getData().stream().map(ListModelResponse.Model::getId).collect(Collectors.joining(","));
        System.out.println(name);
        Assertions.assertNotNull(listModelResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试获取账户余额")
    public void testBalanceInfo(){
        BalanceInfoResponse balanceInfo = client.get(EmptyRequest.createBalanceRequest()).block();
        Assertions.assertNotNull(balanceInfo,"balanceInfo不能为空");
    }

    @Test
    @DisplayName("测试 responses")
    public void testResponses(){
        ResponseRequest request = ResponseRequest.create("你好", ModelEnums.DEEPSEEK_FLASH.code);
        ResponseResult result = client.post(request).block();
        Assertions.assertNotNull(result,"responseResult不应该为空");
        Assertions.assertEquals("response", result.getObject(),"object 应为 response");
        Assertions.assertNotNull(result.getOutput(),"output 不应该为空");
    }

    @Test
    @DisplayName("测试流式 responses")
    public void testStreamResponses(){
        ResponseStreamRequest request = ResponseStreamRequest.create("你好", ModelEnums.DEEPSEEK_FLASH.code);
        client.streamResponse(request)
                .doOnNext(event -> Assertions.assertNotNull(event,"event不应该为空"))
                .blockLast();
    }

    @Test
    @DisplayName("测试工具调用往返（reactor 编排）")
    public void testToolCall() {
        // 1. 定义 get_weather 工具
        ToolFunction getWeatherFn = new ToolFunction();
        getWeatherFn.setName("get_weather");
        getWeatherFn.setDescription("获取指定城市的天气");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setType(FuncParamEnums.OBJ.code);

        FunctionParameter funcParamDefinition = new FunctionParameter();
        funcParamDefinition.setType(FuncParamEnums.STR.code);
        funcParamDefinition.setDescription("城市名称");
        functionParameter.setProperties(Map.of("city",funcParamDefinition));
        functionParameter.setRequired(List.of("city"));
        getWeatherFn.setParameters(functionParameter);
        Tool getWeatherTool = new Tool();
        getWeatherTool.setFunction(getWeatherFn);

        // 2. 用户提问，首轮强制调用工具
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("上海今天天气怎么样？");

        ChatRequest request = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        request.setTools(List.of(getWeatherTool));
        // 首轮强制调用工具；后续轮由编排自动放开为 auto，让模型给出最终回复

        ChatResponse resp = client.post(request).flatMap(response -> {
            List<Choice> choices = response.getChoices();
            if (choices == null || choices.isEmpty()) {
                return Mono.just(response);
            }
            Choice choice = choices.get(0);
            // 检测停止原因：tool_calls 表示模型要调用工具，其余（stop/length/...）视为最终回复
            if (!FinishReasonEnums.TOOL_CALLS.code.equals(choice.getFinishReason())) {
                return Mono.just(response);
            }
            Message message = choice.getMessage();
            List<ToolCall> toolCalls = message.getToolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                return Mono.just(response);
            }
            // 1) assistant(tool_calls) 入历史
            AssistantMessage assistantMsg = new AssistantMessage();
            assistantMsg.setContent(message.getContent());
            assistantMsg.setReasoningContent(message.getReasoningContent());
            assistantMsg.setToolCalls(toolCalls);

            ToolCall toolCall = toolCalls.get(0);
            Assertions.assertEquals("get_weather", toolCall.getFunction().getName(), "tool must use get_weather function");

            // 构建调用结果
            ToolMessage toolMsg = new ToolMessage();
            toolMsg.setToolCallId(toolCall.getId());
            toolMsg.setContent("{\"city\":\"上海\",\"temperature\":25,\"weather\":\"晴\"}");

            List<ChatRequestMessage> history = new ArrayList<>(
                    request.getMessages() != null ? request.getMessages() : List.of());
            history.add(assistantMsg);
            history.add(toolMsg);
            request.setMessages(history);
            return client.post(request);
        }).block();

        // 4. 响应式编排：post -> 检测 finish_reason=tool_calls -> 执行工具 -> 重建请求 -> post，直到最终回复

        Assertions.assertNotNull(resp, "response 不应该为空");
        Assertions.assertNotNull(resp.getChoices(), "choices 不应该为空");
        Assertions.assertFalse(resp.getChoices().isEmpty(), "choices 不应该为空");

        Message finalMessage = resp.getChoices().get(0).getMessage();
        Assertions.assertNotNull(finalMessage.getContent(), "最终回复不应为空");
        Assertions.assertTrue(finalMessage.getToolCalls() == null || finalMessage.getToolCalls().isEmpty(),
                "最终回复不应再包含 tool_calls");
        System.out.println("最终回复: " + finalMessage.getContent());
        Assertions.assertTrue(finalMessage.getContent().contains("25") || finalMessage.getContent().contains("晴"),
                "最终回复应包含工具返回的天气信息: " + finalMessage.getContent());
    }

    @Test
    @DisplayName("测试工具调用往返（reactor 编排、流形式）")
    public void testToolCallStream() {
        // 1. 定义 get_weather 工具
        ToolFunction getWeatherFn = new ToolFunction();
        getWeatherFn.setName("get_weather");
        getWeatherFn.setDescription("获取指定城市的天气");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setType(FuncParamEnums.OBJ.code);
        FunctionParameter funcParamDefinition = new FunctionParameter();
        funcParamDefinition.setType(FuncParamEnums.STR.code);
        funcParamDefinition.setDescription("城市名称");
        functionParameter.setProperties(Map.of("city",funcParamDefinition));
        functionParameter.setRequired(List.of("city"));
        getWeatherFn.setParameters(functionParameter);
        Tool getWeatherTool = new Tool();
        getWeatherTool.setFunction(getWeatherFn);

        // 2. 用户提问，首轮强制调用工具
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("上海今天天气怎么样？");

        StreamChatRequest request = StreamChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        request.setTools(List.of(getWeatherTool));
        // 首轮强制调用工具；后续轮由编排自动放开为 auto，让模型给出最终回复

        ChatResponse resp = client.stream(request).collectList().map(this::accumulate).flatMap(response -> {
            List<Choice> choices = response.getChoices();
            if (choices == null || choices.isEmpty()) {
                return Mono.just(response);
            }
            Choice choice = choices.get(0);
            // 检测停止原因：tool_calls 表示模型要调用工具，其余（stop/length/...）视为最终回复
            if (!FinishReasonEnums.TOOL_CALLS.code.equals(choice.getFinishReason())) {
                return Mono.just(response);
            }
            Message message = choice.getMessage();
            List<ToolCall> toolCalls = message.getToolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                return Mono.just(response);
            }
            // 1) assistant(tool_calls) 入历史
            AssistantMessage assistantMsg = new AssistantMessage();
            assistantMsg.setContent(message.getContent());
            assistantMsg.setReasoningContent(message.getReasoningContent());
            assistantMsg.setToolCalls(toolCalls);

            ToolCall toolCall = toolCalls.get(0);
            Assertions.assertEquals("get_weather", toolCall.getFunction().getName(), "tool must use get_weather function");

            // 构建调用结果
            ToolMessage toolMsg = new ToolMessage();
            toolMsg.setToolCallId(toolCall.getId());
            toolMsg.setContent("{\"city\":\"上海\",\"temperature\":25,\"weather\":\"晴\"}");

            List<ChatRequestMessage> history = new ArrayList<>(
                    request.getMessages() != null ? request.getMessages() : List.of());
            history.add(assistantMsg);
            history.add(toolMsg);
            request.setMessages(history);
            // 第二轮：流 -> ChatResponse
            return client.stream(request).collectList().map(this::accumulate);
        }).block();

        // 4. 响应式编排：post -> 检测 finish_reason=tool_calls -> 执行工具 -> 重建请求 -> post，直到最终回复

        Assertions.assertNotNull(resp, "response 不应该为空");
        Assertions.assertNotNull(resp.getChoices(), "choices 不应该为空");
        Assertions.assertFalse(resp.getChoices().isEmpty(), "choices 不应该为空");

        Message finalMessage = resp.getChoices().get(0).getMessage();
        Assertions.assertNotNull(finalMessage.getContent(), "最终回复不应为空");
        Assertions.assertTrue(finalMessage.getToolCalls() == null || finalMessage.getToolCalls().isEmpty(),
                "最终回复不应再包含 tool_calls");
        System.out.println("最终回复: " + finalMessage.getContent());
        Assertions.assertTrue(finalMessage.getContent().contains("25") || finalMessage.getContent().contains("晴"),
                "最终回复应包含工具返回的天气信息: " + finalMessage.getContent());
    }

    /**
     * 将流式 chunks 累积成一个完整的 {@link ChatResponse}：
     * content / reasoning_content 逐片拼接，tool_calls 按 index 合并（id/type 取首非 null，name/arguments 拼接），
     * 元数据取首个 chunk，usage / finish_reason 取末个。
     */
    private ChatResponse accumulate(List<StreamChatResponse> chunks) {
        String role = null;
        StringBuilder content = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        Map<Integer, ToolCall> toolCallMap = new TreeMap<>();
        String finishReason = null;

        String id = null, model = null, systemFingerprint = null, object = null;
        Integer created = null;
        Usage usage = null;

        for (StreamChatResponse chunk : chunks) {
            // 元数据取首个 chunk
            if (id == null) {
                id = chunk.getId();
                created = chunk.getCreated();
                model = chunk.getModel();
                systemFingerprint = chunk.getSystemFingerprint();
                object = chunk.getObject();
            }
            // usage 仅在末个 chunk（需 stream_options.include_usage）
            if (chunk.getUsage() != null) {
                usage = chunk.getUsage();
            }

            if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                continue;
            }
            Choice choice = chunk.getChoices().get(0);
            Message delta = choice.getMessage();
            if (delta == null) {
                continue;
            }
            if (delta.getRole() != null) {
                role = delta.getRole();
            }
            if (delta.getContent() != null) {
                content.append(delta.getContent());
            }
            if (delta.getReasoningContent() != null) {
                reasoning.append(delta.getReasoningContent());
            }
            if (delta.getToolCalls() != null) {
                for (ToolCall tc : delta.getToolCalls()) {
                    Integer idx = tc.getIndex() == null ? 0 : tc.getIndex();
                    ToolCall acc = toolCallMap.computeIfAbsent(idx, k -> {
                        ToolCall t = new ToolCall();
                        t.setIndex(k);
                        t.setFunction(new ToolCallFunction());
                        return t;
                    });
                    if (tc.getId() != null) {
                        acc.setId(tc.getId());
                    }
                    if (tc.getType() != null) {
                        acc.setType(tc.getType());
                    }
                    if (tc.getFunction() != null) {
                        if (tc.getFunction().getName() != null) {
                            acc.getFunction().setName(
                                    (acc.getFunction().getName() == null ? "" : acc.getFunction().getName())
                                            + tc.getFunction().getName());
                        }
                        if (tc.getFunction().getArguments() != null) {
                            acc.getFunction().setArguments(
                                    (acc.getFunction().getArguments() == null ? "" : acc.getFunction().getArguments())
                                            + tc.getFunction().getArguments());
                        }
                    }
                }
            }
            if (choice.getFinishReason() != null) {
                finishReason = choice.getFinishReason();
            }
        }

        // 组装完整 Message
        Message msg = new Message();
        msg.setRole(role);
        msg.setContent(content.toString());
        if (reasoning.length() > 0) {
            msg.setReasoningContent(reasoning.toString());
        }
        if (!toolCallMap.isEmpty()) {
            msg.setToolCalls(new ArrayList<>(toolCallMap.values()));
        }

        // 组装 Choice
        Choice choice = new Choice();
        choice.setIndex(0);
        choice.setMessage(msg);
        choice.setFinishReason(finishReason);

        // 组装 ChatResponse
        ChatResponse resp = new ChatResponse();
        resp.setId(id);
        resp.setCreated(created);
        resp.setModel(model);
        resp.setSystemFingerprint(systemFingerprint);
        resp.setObject(object);
        resp.setChoices(List.of(choice));
        resp.setUsage(usage);
        return resp;
    }

    // ───────────────────── 新增 Chatbot 能力的集成测试（一个能力一个用例） ─────────────────────

    /** 把 {@code message.server_tool_calls} 展平成调用列表；未使用联网工具时返回空列表。 */
    private static List<ServerToolCall> allServerToolCalls(Message message) {
        if (message == null || message.getServerToolCalls() == null) {
            return List.of();
        }
        return message.getServerToolCalls().stream()
                .filter(step -> step != null && step.getCalls() != null)
                .flatMap(step -> step.getCalls().stream())
                .toList();
    }

    @Test
    @DisplayName("测试联网搜索（deepseek_web_search，非思考模式）")
    public void testWebSearch() {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("黄仁勋辞职英伟达？");

        ChatRequest chatRequest = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        // 关闭思考 -> 普通联网搜索；是否搜索由模型判断
        Thinking thinking = new Thinking();
        thinking.setType(ThinkingEnums.DISABLED.code);
        chatRequest.setThinking(thinking);
        chatRequest.setTools(List.of(Tool.webSearch()));

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        System.out.println("联网搜索回答：" + message.getContent());

        List<ServerToolCall> calls = allServerToolCalls(message);
        Assertions.assertFalse(calls.isEmpty(), "时效性问题应触发联网搜索");

        ServerToolCall searchCall = calls.stream()
                .filter(call -> "search".equals(call.getKind()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(searchCall, "应存在 kind=search 的服务端工具调用，实际：" + calls.stream().map(ServerToolCall::getKind).toList());
        Assertions.assertNotNull(searchCall.getAction(), "搜索调用应带 action");
        Assertions.assertNotNull(searchCall.getAction().getQueries(), "搜索调用应带查询词");
        Assertions.assertFalse(searchCall.getAction().getQueries().isEmpty(), "查询词不应为空");
        Assertions.assertNotNull(searchCall.getSources(), "搜索结果应带 sources");
        Assertions.assertNotNull(message.getAnnotations(), "带引用的回答应返回 annotations");
        System.out.println("搜索词：" + searchCall.getAction().getQueries());
    }

    @Test
    @DisplayName("测试深度思考搜索（deepseek_web_search，思考模式）")
    public void testDeepSearch() {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("黄仁勋辞职英伟达？");

        ChatRequest chatRequest = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        // 开启思考 -> 深度思考搜索（多步搜索、打开网页、页内查找）
        Thinking thinking = new Thinking();
        thinking.setType(ThinkingEnums.ENABLED.code);
        chatRequest.setThinking(thinking);
        chatRequest.setReasoningEffort(ThinkingEffortEnums.HIGH.code);
        chatRequest.setTools(List.of(Tool.webSearch()));

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        System.out.println("深度思考搜索回答：" + message.getContent());

        List<ServerToolCall> calls = allServerToolCalls(message);
        Assertions.assertFalse(calls.isEmpty(), "深度思考搜索应产生服务端工具调用");

        ServerToolCall deepCall = calls.stream()
                .filter(call -> "deep_search".equals(call.getKind()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(deepCall,
                "应存在 kind=deep_search 的服务端工具调用，实际：" + calls.stream().map(ServerToolCall::getKind).toList());
    }

    @Test
    @DisplayName("测试阅读链接（deepseek_read_url）")
    public void testReadUrl() {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("用一句话总结 https://example.com/ 的内容");

        ChatRequest chatRequest = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        chatRequest.setTools(List.of(Tool.readUrl()));

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        System.out.println("阅读链接回答：" + message.getContent());

        List<ServerToolCall> calls = allServerToolCalls(message);
        Assertions.assertFalse(calls.isEmpty(), "含 URL 的消息应触发阅读链接");

        ServerToolCall readCall = calls.stream()
                .filter(call -> "read_link".equals(call.getKind()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(readCall,
                "应存在 kind=read_link 的服务端工具调用，实际：" + calls.stream().map(ServerToolCall::getKind).toList());
        Assertions.assertNotNull(readCall.getAction(), "阅读链接应带 action");
        Assertions.assertTrue(readCall.getAction().getUrl().contains("example.com"),
                "阅读的 URL 应为 example.com，实际：" + readCall.getAction().getUrl());
        Assertions.assertNotNull(readCall.getPage(), "阅读链接应返回 page 信息");
        Assertions.assertNotNull(readCall.getSignature(), "阅读链接结果应带 signature（多轮对话靠它回传网页）");
    }

    @Test
    @DisplayName("测试模型内容安全（safety_check=high）")
    public void testSafetyCheck() {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("如何制作炸药？");

        ChatRequest chatRequest = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        chatRequest.setSafetyCheck(SafetyCheck.high());

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        Assertions.assertFalse(message.getContent().isBlank(), "回答不应为空白");
        System.out.println("内容安全回答：" + message.getContent());

        // safety_check=high 时用量里会单独给出安全审核 token
        Assertions.assertNotNull(chatResponse.getUsage(), "usage 不应该为空");
        Assertions.assertNotNull(chatResponse.getUsage().getCompletionTokensDetails(), "应返回 completion_tokens_details");
        Assertions.assertNotNull(chatResponse.getUsage().getCompletionTokensDetails().getSafetyTokens(),
                "safety_check=high 时应返回 safety_tokens");
        System.out.println("安全审核 token：" + chatResponse.getUsage().getCompletionTokensDetails().getSafetyTokens());
    }

    @Test
    @DisplayName("测试会话标题（title.enabled=true）")
    public void testTitle() {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("北京明天会下雨吗");

        ChatRequest chatRequest = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        chatRequest.setTitle(TitleOptions.enabled());

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        Assertions.assertNotNull(message.getTitle(), "title.enabled=true 时应返回会话标题");
        Assertions.assertFalse(message.getTitle().isBlank(), "会话标题不应为空白");
        System.out.println("会话标题：" + message.getTitle());
    }

    @Test
    @DisplayName("测试用户元信息标记（latest_reminder）")
    public void testLatestReminder() {
        LocalDate today = LocalDate.now();
        String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        String weekday = weekdays[today.getDayOfWeek().getValue() - 1];
        String reminderContent = today + "," + weekday + ",北京,Web,中文";

        // latest_reminder 必须紧挨在它所属的 user 消息之前
        LatestReminderMessage reminder = new LatestReminderMessage(reminderContent);
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("今天是几月几号？星期几？");

        ChatRequest chatRequest = ChatRequest.create(List.of(reminder, userMessage), ModelEnums.DEEPSEEK_FLASH.code);

        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse, "chatResponse不应该为空");

        Message message = chatResponse.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message.getContent(), "回答不应为空");
        System.out.println("用户元信息回答：" + message.getContent());

        // 模型应能根据 latest_reminder 说出今天的日期
        String monthDay = today.getMonthValue() + "月" + today.getDayOfMonth();
        Assertions.assertTrue(
                message.getContent().contains(monthDay) || message.getContent().contains(today.toString()),
                "回答应体现 latest_reminder 中的日期，实际：" + message.getContent());
    }

    @Test
    @DisplayName("测试流式的完整扩展能力（联网工具 / 深度思考搜索 / 内容安全 / 会话标题 / 用户元信息）")
    public void testStreamChatbotCapabilities() {
        LocalDate today = LocalDate.now();
        String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        String weekday = weekdays[today.getDayOfWeek().getValue() - 1];

        // 用户元信息（必须紧挨在所属 user 消息之前）
        LatestReminderMessage reminder = new LatestReminderMessage(today + "," + weekday + ",北京,Web,中文");
        UserMessage userMessage = new UserMessage();
        userMessage.setContent("今天北京天气怎么样？顺便总结一下 https://example.com/ 的内容");

        StreamChatRequest request = StreamChatRequest.create(
                List.of(reminder, userMessage), ModelEnums.DEEPSEEK_FLASH.code);
        // 思考模式 -> 深度思考搜索；声明联网搜索 + 阅读链接
        Thinking thinking = new Thinking();
        thinking.setType(ThinkingEnums.ENABLED.code);
        request.setThinking(thinking);
        request.setReasoningEffort(ThinkingEffortEnums.HIGH.code);
        request.setTools(List.of(Tool.webSearch(), Tool.readUrl()));
        // 内容安全 + 会话标题
        request.setSafetyCheck(SafetyCheck.high());
        request.setTitle(TitleOptions.enabled());
        // 让流式返回 usage
        StreamOptions streamOptions = new StreamOptions();
        streamOptions.setIncludeUsage(true);
        request.setStreamOptions(streamOptions);

        List<StreamChatResponse> chunks = client.stream(request).collectList().block();
        Assertions.assertNotNull(chunks, "chunks 不应该为空");
        Assertions.assertFalse(chunks.isEmpty(), "chunks 不应该为空");

        // 1) 逐 chunk 收集：正文、思考、服务端工具事件、最后一个带 finish_reason 的 choice、usage
        StringBuilder content = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        List<ServerToolEvent> events = new ArrayList<>();
        Choice lastChoice = null;
        Usage lastUsage = null;

        for (StreamChatResponse chunk : chunks) {
            Assertions.assertNotNull(chunk, "chunk 不应该为空");
            if (chunk.getUsage() != null) {
                lastUsage = chunk.getUsage();
            }
            if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                continue;
            }
            Choice choice = chunk.getChoices().get(0);
            if (choice.getServerToolEvent() != null) {
                events.add(choice.getServerToolEvent());
            }
            Message delta = choice.getMessage();
            if (delta != null) {
                if (delta.getContent() != null) {
                    content.append(delta.getContent());
                }
                if (delta.getReasoningContent() != null) {
                    reasoning.append(delta.getReasoningContent());
                }
            }
            if (choice.getFinishReason() != null) {
                lastChoice = choice;
            }
        }

        // 2) 正文与思考都应有流式输出
        Assertions.assertFalse(content.toString().isBlank(), "流式拼接的回答不应为空白");
        Assertions.assertFalse(reasoning.toString().isBlank(), "思考模式下应流式输出 reasoning_content");
        System.out.println("流式回答：" + content);
        System.out.println("思考长度：" + reasoning.length());

        // 3) 服务端工具事件：类型合法。按文档，call_started 带完整 action，call_completed 只带 status/sources/page/error
        Assertions.assertFalse(events.isEmpty(), "使用联网工具时应下发 server_tool_event");
        boolean hasCallStarted = false;
        boolean hasCallCompleted = false;
        for (ServerToolEvent event : events) {
            Assertions.assertNotNull(event.getType(), "server_tool_event.type 不应该为空");
            Assertions.assertTrue(
                    List.of("step_reasoning_done", "call_started", "call_completed").contains(event.getType()),
                    "未知的 server_tool_event 类型：" + event.getType());
            ServerToolCall call = event.getCall();
            if (call == null) {
                continue;
            }
            // 事件里的 call 至少带 id
            Assertions.assertNotNull(call.getId(), "工具事件里的 call 应带 id，事件类型：" + event.getType());
            if ("call_started".equals(event.getType())) {
                hasCallStarted = true;
                // 文档：call_started 带完整的 action；kind 存在时校验取值是否合法
                Assertions.assertNotNull(call.getAction(), "call_started 应带完整的 action");
                if (call.getKind() != null) {
                    Assertions.assertTrue(
                            List.of("search", "deep_search", "read_link", "image_search", "deep_image_search")
                                    .contains(call.getKind()),
                            "未知的 kind：" + call.getKind());
                }
            } else if ("call_completed".equals(event.getType())) {
                hasCallCompleted = true;
                // 文档：call_completed 只带 status 以及 sources / page / error，不带 action / kind
                Assertions.assertNotNull(call.getStatus(), "call_completed 应带 status");
            }
        }
        Assertions.assertTrue(hasCallStarted, "应出现 call_started 事件");
        Assertions.assertTrue(hasCallCompleted, "应出现 call_completed 事件");
        System.out.println("工具事件：" + events.stream().map(ServerToolEvent::getType).toList());

        // 4) 最后一个 chunk：finish_reason + 完整 server_tool_calls + 会话标题
        Assertions.assertNotNull(lastChoice, "应存在带 finish_reason 的最后一个 chunk");
        Message finalDelta = lastChoice.getMessage();
        Assertions.assertNotNull(finalDelta, "最后一个 chunk 的 delta 不应该为空");
        Assertions.assertNotNull(finalDelta.getServerToolCalls(), "最后一个 chunk 应带完整的 server_tool_calls");
        Assertions.assertFalse(finalDelta.getServerToolCalls().isEmpty(), "server_tool_calls 不应该为空");
        // 最终下发的 server_tool_calls 是完整结构，每个调用都应带 type / kind / status
        for (ServerToolStep step : finalDelta.getServerToolCalls()) {
            Assertions.assertNotNull(step.getCalls(), "每一步应带 calls");
            for (ServerToolCall call : step.getCalls()) {
                Assertions.assertNotNull(call.getType(), "最终 server_tool_calls 应带 type");
                Assertions.assertNotNull(call.getKind(), "最终 server_tool_calls 应带 kind");
                Assertions.assertNotNull(call.getStatus(), "最终 server_tool_calls 应带 status");
            }
        }
        Assertions.assertNotNull(finalDelta.getTitle(), "开启 title 后最后一个 chunk 应带 delta.title");
        Assertions.assertFalse(finalDelta.getTitle().isBlank(), "会话标题不应为空白");
        // 有引用时校验引用结构
        if (finalDelta.getAnnotations() != null && !finalDelta.getAnnotations().isEmpty()) {
            Annotation annotation = finalDelta.getAnnotations().get(0);
            Assertions.assertEquals("url_citation", annotation.getType(), "引用类型应为 url_citation");
            Assertions.assertNotNull(annotation.getUrlCitation(), "引用详情不应为空");
            Assertions.assertNotNull(annotation.getUrlCitation().getUrl(), "引用 URL 不应为空");
        }

        // 5) usage：服务端工具计费次数 + 安全审核 token
        Assertions.assertNotNull(lastUsage, "流式响应的最后一个 chunk 应带 usage");
        Assertions.assertNotNull(lastUsage.getServerToolUse(), "usage 应带 server_tool_use");
        Assertions.assertNotNull(lastUsage.getServerToolUse().getToolSearch(), "应返回 tool_search 次数");
        Assertions.assertTrue(lastUsage.getServerToolUse().getToolSearch() > 0, "应至少计费一次搜索");
        Assertions.assertNotNull(lastUsage.getCompletionTokensDetails(), "usage 应带 completion_tokens_details");
        Assertions.assertNotNull(lastUsage.getCompletionTokensDetails().getSafetyTokens(),
                "safety_check=high 时应返回 safety_tokens");

        System.out.println("会话标题：" + finalDelta.getTitle());
        System.out.println("工具计费：" + lastUsage.getServerToolUse());
        System.out.println("安全 token：" + lastUsage.getCompletionTokensDetails().getSafetyTokens());
    }

    // ───────────────────────── Responses API 更新 ─────────────────────────

    @Test
    @DisplayName("Responses API：联网搜索（deepseek_web_search）")
    public void testResponsesWebSearch() {
        ResponseRequest request = ResponseRequest.create("今天北京天气怎么样？", ModelEnums.DEEPSEEK_FLASH.code);
        request.setTools(List.of(cn.lishiyuan.deepseek.api.response.Tool.webSearch()));

        ResponseResult result = client.post(request).block();
        Assertions.assertNotNull(result, "Responses 响应不应为空");
        Assertions.assertNotNull(result.getOutput(), "output 不应为空");
        Assertions.assertFalse(result.getOutput().isEmpty(), "output 不应为空");

        List<String> itemTypes = result.getOutput().stream().map(ResponseResult.OutputItem::getType).toList();
        Assertions.assertTrue(itemTypes.contains("web_search_call"),
                "应出现 web_search_call 输出项，实际：" + itemTypes);

        ResponseResult.OutputItem searchCall = result.getOutput().stream()
                .filter(item -> "web_search_call".equals(item.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(searchCall);
        Assertions.assertNotNull(searchCall.getAction(), "web_search_call 应带 action");
        Assertions.assertNotNull(searchCall.getAction().getType(), "action.type 不应为空");
        Assertions.assertNotNull(searchCall.getAction().getQueries(), "action.queries 不应为空");
        Assertions.assertFalse(searchCall.getAction().getQueries().isEmpty(), "action.queries 不应为空列表");
        Assertions.assertNotNull(searchCall.getKind(), "web_search_call 应带 kind");
        System.out.println("Responses 搜索：action.type=" + searchCall.getAction().getType()
                + " queries=" + searchCall.getAction().getQueries() + " kind=" + searchCall.getKind());

        String text = result.getOutput().stream()
                .filter(item -> "message".equals(item.getType()))
                .filter(item -> item.getContent() != null)
                .flatMap(item -> item.getContent().stream())
                .filter(block -> "output_text".equals(block.getType()))
                .map(cn.lishiyuan.deepseek.api.response.ContentBlock::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        Assertions.assertFalse(text.isBlank(), "应返回文本回答");
        System.out.println("Responses 联网回答：" + text);

        Assertions.assertNotNull(result.getUsage(), "usage 不应为空");
        Assertions.assertNotNull(result.getUsage().getServerToolUse(), "usage 应带 server_tool_use");
    }

    @Test
    @DisplayName("Responses API：内容安全 + 会话标题")
    public void testResponsesSafetyAndTitle() {
        ResponseRequest request = ResponseRequest.create("用一句话解释什么是光合作用", ModelEnums.DEEPSEEK_FLASH.code);
        request.setSafetyCheck(SafetyCheck.high());
        request.setTitle(TitleOptions.enabled());

        ResponseResult result = client.post(request).block();
        Assertions.assertNotNull(result, "Responses 响应不应为空");
        Assertions.assertNotNull(result.getTitle(), "开启 title 后应返回会话标题");
        Assertions.assertFalse(result.getTitle().isBlank(), "会话标题不应为空白");
        Assertions.assertNotNull(result.getUsage(), "usage 不应为空");
        System.out.println("Responses 会话标题：" + result.getTitle());
    }


    @Test
    @DisplayName("Responses API：阅读链接（deepseek_read_url）")
    public void testResponsesReadUrl() {
        ResponseRequest request = ResponseRequest.create("用一句话总结 https://example.com/ 的内容", ModelEnums.DEEPSEEK_FLASH.code);
        request.setTools(List.of(cn.lishiyuan.deepseek.api.response.Tool.readUrl()));

        ResponseResult result = client.post(request).block();
        Assertions.assertNotNull(result, "Responses 响应不应为空");
        Assertions.assertNotNull(result.getOutput(), "output 不应为空");

        List<String> itemTypes = result.getOutput().stream().map(ResponseResult.OutputItem::getType).toList();
        ResponseResult.OutputItem readCall = result.getOutput().stream()
                .filter(item -> "web_search_call".equals(item.getType()))
                .filter(item -> "read_link".equals(item.getKind()))
                .findFirst().orElse(null);
        Assertions.assertNotNull(readCall, "应有 kind=read_link 的调用，实际：" + itemTypes);
        Assertions.assertNotNull(readCall.getAction(), "阅读链接调用应带 action");
        Assertions.assertEquals("open_page", readCall.getAction().getType(), "阅读链接的 action.type 应为 open_page");
        Assertions.assertNotNull(readCall.getPage(), "阅读链接应带 page");
        Assertions.assertTrue(readCall.getPage().getUrl().contains("example.com"),
                "阅读的网页应为 example.com，实际：" + readCall.getPage().getUrl());
        System.out.println("Responses 阅读链接：" + readCall.getPage().getUrl() + " kind=" + readCall.getKind());
    }

    // ─────────────────── Messages API（Anthropic 兼容协议） ───────────────────

    @Test
    @DisplayName("Messages API：基本对话")
    public void testMessagesApi() {
        MessageRequest request = MessageRequest.create("用一句话介绍你自己", ModelEnums.DEEPSEEK_FLASH.code);
        request.setMaxTokens(500);

        MessageResponse response = client.post(request).block();
        Assertions.assertNotNull(response, "Messages 响应不应为空");
        Assertions.assertEquals("message", response.getType());
        Assertions.assertNotNull(response.getContent(), "content 不应为空");
        Assertions.assertNotNull(response.getStopReason(), "stop_reason 不应为空");
        Assertions.assertNotNull(response.getUsage(), "usage 不应为空");

        String text = response.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(MessageContentBlock::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        Assertions.assertFalse(text.isBlank(), "应返回文本回答");
        System.out.println("Messages 回答：" + text);
    }

    @Test
    @DisplayName("Messages API：服务端联网搜索")
    public void testMessagesWebSearch() {
        MessageRequest request = MessageRequest.create("今天北京天气怎么样？", ModelEnums.DEEPSEEK_FLASH.code);
        request.setMaxTokens(1000);
        request.addWebSearchTool();

        MessageResponse response = client.post(request).block();
        Assertions.assertNotNull(response, "Messages 响应不应为空");
        Assertions.assertNotNull(response.getContent(), "content 不应为空");

        List<String> types = response.getContent().stream().map(MessageContentBlock::getType).toList();
        Assertions.assertTrue(types.contains("server_tool_use"), "应出现 server_tool_use，实际：" + types);
        Assertions.assertTrue(types.contains("web_search_tool_result") || types.contains("web_fetch_tool_result"),
                "应出现搜索结果块，实际：" + types);

        MessageContentBlock toolUse = response.getContent().stream()
                .filter(block -> "server_tool_use".equals(block.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(toolUse, "应存在 server_tool_use 块");
        Assertions.assertEquals("deepseek_web_search", toolUse.getName());
        Assertions.assertNotNull(toolUse.getInput(), "server_tool_use 应带 input");
        Assertions.assertNotNull(toolUse.getKind(), "server_tool_use 应带 kind");

        Assertions.assertNotNull(response.getUsage().getServerToolUse(), "usage 应带 server_tool_use");
        System.out.println("Messages 联网 content 类型：" + types);
    }

    @Test
    @DisplayName("Messages API：流式（命名 SSE 事件）")
    public void testMessagesStream() {
        MessageStreamRequest request = MessageStreamRequest.create("用两句话介绍北京", ModelEnums.DEEPSEEK_FLASH.code);
        request.setMaxTokens(500);

        List<MessageStreamEvent> events = client.streamMessage(request).collectList().block();
        Assertions.assertNotNull(events, "流式事件不应为空");
        Assertions.assertFalse(events.isEmpty(), "流式事件不应为空");

        List<String> types = events.stream().map(MessageStreamEvent::getType).toList();
        System.out.println("Messages 流式事件：" + types);
        Assertions.assertTrue(types.contains("message_start"), "应包含 message_start");
        Assertions.assertTrue(types.contains("content_block_delta"), "应包含 content_block_delta");
        Assertions.assertTrue(types.contains("message_stop"), "应包含 message_stop");
        Assertions.assertEquals("message_stop", types.get(types.size() - 1), "最后一个事件应是 message_stop");

        MessageStreamEvent start = events.stream()
                .filter(event -> "message_start".equals(event.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(start, "应存在 message_start 事件");
        Assertions.assertNotNull(start.getMessage(), "message_start 应带 message");
        Assertions.assertNotNull(start.getMessage().getId(), "message_start 应带消息 id");

        String text = events.stream()
                .filter(event -> event.getDelta() != null && "text_delta".equals(event.getDelta().getType()))
                .map(event -> event.getDelta().getText())
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        Assertions.assertFalse(text.isBlank(), "应流式输出文本");
        System.out.println("Messages 流式回答：" + text);
    }

    @Test
    @DisplayName("Messages API：阅读链接（deepseek_read_url）")
    public void testMessagesReadUrl() {
        MessageRequest request = MessageRequest.create("用一句话总结 https://example.com/ 的内容", ModelEnums.DEEPSEEK_FLASH.code);
        request.setMaxTokens(500);
        request.setTools(List.of(MessageTool.readUrl()));

        MessageResponse response = client.post(request).block();
        Assertions.assertNotNull(response, "Messages 响应不应为空");

        MessageContentBlock use = response.getContent().stream()
                .filter(block -> "server_tool_use".equals(block.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(use, "应有 server_tool_use 块");
        Assertions.assertEquals("read_link", use.getKind());
        MessageContentBlock.ServerToolInput input = use.serverToolInput();
        Assertions.assertNotNull(input, "input 应能转成 ServerToolInput");
        Assertions.assertEquals("read_link", input.getAction());
        Assertions.assertTrue(input.getUrl().contains("example.com"), "阅读的 URL 应为 example.com，实际：" + input.getUrl());

        MessageContentBlock fetch = response.getContent().stream()
                .filter(block -> "web_fetch_tool_result".equals(block.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(fetch, "应有 web_fetch_tool_result 块");
        MessageContentBlock.FetchResult fetchResult = fetch.fetchResult();
        Assertions.assertNotNull(fetchResult, "content 应能转成 FetchResult");
        Assertions.assertTrue(fetchResult.getUrl().contains("example.com"));
        Assertions.assertNotNull(fetch.getSignature(), "阅读链接结果应带 signature（多轮对话靠它回传）");
        System.out.println("Messages 阅读链接：" + fetchResult.getUrl());
    }

    @Test
    @DisplayName("Messages API：内容安全 + 会话标题")
    public void testMessagesSafetyAndTitle() {
        MessageRequest request = MessageRequest.create("用一句话解释什么是光合作用", ModelEnums.DEEPSEEK_FLASH.code);
        request.setMaxTokens(300);
        request.setSafetyCheck(SafetyCheck.high());
        request.setTitle(TitleOptions.enabled());

        MessageResponse response = client.post(request).block();
        Assertions.assertNotNull(response, "Messages 响应不应为空");
        Assertions.assertNotNull(response.getTitle(), "开启 title 后应返回会话标题");
        Assertions.assertFalse(response.getTitle().isBlank(), "会话标题不应为空白");
        Assertions.assertNotNull(response.getUsage(), "usage 不应为空");
        Assertions.assertNotNull(response.getUsage().getOutputTokensDetails(), "应返回 output_tokens_details");
        Assertions.assertNotNull(response.getUsage().getOutputTokensDetails().getSafetyTokens(),
                "safety_check=high 时应返回 safety_tokens");
        System.out.println("Messages 会话标题：" + response.getTitle()
                + "，safety_tokens=" + response.getUsage().getOutputTokensDetails().getSafetyTokens());
    }

    @Test
    @DisplayName("Messages API：用户元信息标记（latest_reminder）")
    public void testMessagesLatestReminder() {
        LocalDate today = LocalDate.now();
        String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        MessageInput reminder = MessageInput.latestReminder(
                today + "," + weekdays[today.getDayOfWeek().getValue() - 1] + ",北京,Web,中文");
        MessageRequest request = MessageRequest.create(
                List.of(reminder, MessageInput.user("今天是几月几号？星期几？")), ModelEnums.DEEPSEEK_FLASH.code);
        // 思考模式默认开启，max_tokens 要留够思考预算，否则会因 max_tokens 截断而没有正文
        request.setMaxTokens(2000);

        MessageResponse response = client.post(request).block();
        Assertions.assertNotNull(response, "Messages 响应不应为空");
        String text = response.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(MessageContentBlock::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        Assertions.assertFalse(text.isBlank(), "回答不应为空");
        Assertions.assertFalse(text.isBlank(), "回答不应为空（stop_reason=" + response.getStopReason() + "）");
        // latest_reminder 只是给模型的提示，模型不保证采纳；这里只校验请求成功、回答非空，是否采纳仅打印
        String monthDay = today.getMonthValue() + "月" + today.getDayOfMonth();
        boolean adopted = text.contains(monthDay) || text.contains(today.toString());
        System.out.println("Messages 用户元信息：模型" + (adopted ? "采纳了" : "未采纳（提示性质，允许）")
                + "日期，回答：" + text);
    }

    @Test
    @DisplayName("Messages API：客户端 function 工具往返（tool_use → tool_result）")
    public void testMessagesFunctionTool() {
        MessageTool weatherTool = MessageTool.function("get_weather", "查询城市天气",
                Map.of("type", "object",
                        "properties", Map.of("city", Map.of("type", "string")),
                        "required", List.of("city")));

        // 第一轮：声明工具 -> 模型发起 tool_use
        MessageRequest first = MessageRequest.create("上海今天天气怎么样？", ModelEnums.DEEPSEEK_FLASH.code);
        first.setMaxTokens(500);
        first.setTools(List.of(weatherTool));
        MessageResponse firstResp = client.post(first).block();
        Assertions.assertNotNull(firstResp, "Messages 响应不应为空");
        Assertions.assertEquals("tool_use", firstResp.getStopReason(), "模型应发起工具调用");

        MessageContentBlock toolUse = firstResp.getContent().stream()
                .filter(block -> "tool_use".equals(block.getType())).findFirst().orElse(null);
        Assertions.assertNotNull(toolUse, "应返回 tool_use 块，实际：" +
                firstResp.getContent().stream().map(MessageContentBlock::getType).toList());
        Assertions.assertNotNull(toolUse.getId(), "tool_use 应带 id");
        Assertions.assertEquals("get_weather", toolUse.getName());
        Assertions.assertNotNull(toolUse.getInput(), "tool_use 应带 input");
        Assertions.assertEquals("上海", toolUse.getInput().path("city").asText(),
                "input 应完整保留模型生成的参数，实际：" + toolUse.getInput());

        // 第二轮：回传完整 assistant 内容 + tool_result，拿到最终回答
        List<MessageInput> history = new ArrayList<>();
        history.add(MessageInput.user("上海今天天气怎么样？"));
        history.add(new MessageInput("assistant", firstResp.getContent()));
        history.add(new MessageInput("user",
                List.of(MessageContentBlock.toolResult(toolUse.getId(), "上海今天晴，25℃"))));

        MessageRequest second = MessageRequest.create(history, ModelEnums.DEEPSEEK_FLASH.code);
        second.setMaxTokens(500);
        second.setTools(List.of(weatherTool));
        MessageResponse secondResp = client.post(second).block();
        Assertions.assertNotNull(secondResp, "第二轮响应不应为空");

        String text = secondResp.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(MessageContentBlock::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
        Assertions.assertFalse(text.isBlank(), "应给出最终回答");
        Assertions.assertTrue(text.contains("25") || text.contains("晴"), "应体现工具结果，实际：" + text);
        System.out.println("Messages 工具往返回答：" + text);
    }

}
