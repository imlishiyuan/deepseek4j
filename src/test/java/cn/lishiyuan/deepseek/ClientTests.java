package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.EmptyRequest;
import cn.lishiyuan.deepseek.api.chat.ChatRequest;
import cn.lishiyuan.deepseek.api.chat.ChatRequestMessage;
import cn.lishiyuan.deepseek.api.chat.StreamChatRequest;
import cn.lishiyuan.deepseek.api.fim.StreamFimRequest;
import cn.lishiyuan.deepseek.api.response.ResponseRequest;
import cn.lishiyuan.deepseek.api.response.ResponseResult;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.api.fim.FimRequest;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.fim.FimResponse;
import cn.lishiyuan.deepseek.api.platform.ListModelResponse;
import cn.lishiyuan.deepseek.config.enums.RoleEnums;
import cn.lishiyuan.deepseek.api.platform.BalanceInfoResponse;
import cn.lishiyuan.deepseek.config.enums.ThinkingEffortEnums;
import cn.lishiyuan.deepseek.config.enums.ThinkingEnums;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        ChatRequestMessage systemMessage = new ChatRequestMessage();
        systemMessage.setRole(RoleEnums.SYSTEM.code);
        systemMessage.setContent("You are a helpful assistant");

        ChatRequestMessage userMessage = new ChatRequestMessage();
        userMessage.setRole(RoleEnums.USER.code);
        userMessage.setContent("请仿照《沁园春·长沙》写一篇词，题为《沁园春·西安》");

        List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
        ChatRequest.Thinking thinking = new ChatRequest.Thinking();
        thinking.setType(ThinkingEnums.ENABLED.code);
        thinking.setReasoningEffort(ThinkingEffortEnums.MAX.code);
        chatRequest.setThinking(thinking);
        ChatResponse chatResponse = client.post(chatRequest).block();
        Assertions.assertNotNull(chatResponse,"chatResponse不应该为空");
    }

    @Test
    @DisplayName("测试流对话")
    public void testStreamChat(){
        ChatRequestMessage systemMessage = new ChatRequestMessage();
        systemMessage.setRole(RoleEnums.SYSTEM.code);
        systemMessage.setContent("You are a helpful assistant");

        ChatRequestMessage userMessage = new ChatRequestMessage();
        userMessage.setRole(RoleEnums.USER.code);
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
}
