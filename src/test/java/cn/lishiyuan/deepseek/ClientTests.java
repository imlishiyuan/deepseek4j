package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.EmptyRequest;
import cn.lishiyuan.deepseek.api.chat.*;
import cn.lishiyuan.deepseek.api.common.FuncParamDefinition;
import cn.lishiyuan.deepseek.api.fim.StreamFimRequest;
import cn.lishiyuan.deepseek.api.response.ResponseRequest;
import cn.lishiyuan.deepseek.api.response.ResponseResult;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.enums.*;
import cn.lishiyuan.deepseek.api.fim.FimRequest;
import cn.lishiyuan.deepseek.api.fim.FimResponse;
import cn.lishiyuan.deepseek.api.platform.ListModelResponse;
import cn.lishiyuan.deepseek.api.platform.BalanceInfoResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@DisplayName("Client测试")
public class ClientTests {

    private static Client client;

    @BeforeAll
    public static void initEnv(){
        // read ackey
        Path path = Paths.get(ClientTests.class.getClassLoader().getResource("accessKey.txt").getPath());
        try {
            String accessKey = Files.readString(path);
            client = new DefaultClient(accessKey);
        } catch (IOException e) {
            throw new RuntimeException("读取accessKey失败",e);
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
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
        Thinking thinking = new Thinking();
        thinking.setType(ThinkingEnums.ENABLED.code);
        thinking.setReasoningEffort(ThinkingEffortEnums.MAX.code);
        chatRequest.setThinking(thinking);
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
        StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);

        client.stream(chatRequest)
                .doOnNext(chatResponse -> Assertions.assertNotNull(chatResponse,"chatResponse"))
                .blockLast();
    }

    @Test
    @DisplayName("测试FIM")
    public void testFIM(){
        FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_V4_FLASH.code);
        FimResponse fimResponse = client.post(fimRequest).block();
        Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
    }

    @Test
    @DisplayName("测试流FIM")
    public void testStreamFIM() {
        StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_V4_FLASH.code);
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
        ResponseRequest request = ResponseRequest.create("你好", ModelEnums.DEEPSEEK_V4_FLASH.code);
        ResponseResult result = client.post(request).block();
        Assertions.assertNotNull(result,"responseResult不应该为空");
        Assertions.assertEquals("response", result.getObject(),"object 应为 response");
        Assertions.assertNotNull(result.getOutput(),"output 不应该为空");
    }

    @Test
    @DisplayName("测试流式 responses")
    public void testStreamResponses(){
        ResponseStreamRequest request = ResponseStreamRequest.create("你好", ModelEnums.DEEPSEEK_V4_FLASH.code);
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
        FuncParamDefinition funcParamDefinition = new FuncParamDefinition();
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

        ChatRequest request = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_V4_FLASH.code);
        request.setTools(List.of(getWeatherTool));
        // 首轮强制调用工具；后续轮由编排自动放开为 auto，让模型给出最终回复

        ChatResponse resp = Objects.requireNonNull(client.post(request).map(response -> {
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
        }).block()).block();

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

}
