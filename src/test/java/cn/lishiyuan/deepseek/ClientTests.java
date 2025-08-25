package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.EmptyRequest;
import cn.lishiyuan.deepseek.api.chat.ChatRequest;
import cn.lishiyuan.deepseek.api.chat.ChatRequestMessage;
import cn.lishiyuan.deepseek.api.chat.StreamChatRequest;
import cn.lishiyuan.deepseek.api.fim.StreamFimRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.api.fim.FimRequest;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.fim.FimResponse;
import cn.lishiyuan.deepseek.api.platform.ListModelResponse;
import cn.lishiyuan.deepseek.config.enums.RoleEnums;
import cn.lishiyuan.deepseek.api.platform.BalanceInfoResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            client = new DefualtClient(accessKey);
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
        userMessage.setContent("你好");

        List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
        ChatResponse chatResponse = client.post(chatRequest);
        Assertions.assertNotNull(chatResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试流对话")
    public void testStreamChat(){
        ChatRequestMessage systemMessage = new ChatRequestMessage();
        systemMessage.setRole(RoleEnums.SYSTEM.code);
        systemMessage.setContent("You are a helpful assistant");

        ChatRequestMessage userMessage = new ChatRequestMessage();
        userMessage.setRole(RoleEnums.USER.code);
        userMessage.setContent("你好");

        List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
        StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
        CountDownLatch latch = new CountDownLatch(1);

        client.stream(chatRequest,chatResponse -> {
            Assertions.assertNotNull(chatResponse,"chatResponse");
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("测试FIM")
    public void testFIM(){
        FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
        FimResponse fimResponse = client.post(fimRequest);
        Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
    }

    @Test
    @DisplayName("测试流FIM")
    public void testStreamFIM() {
        CountDownLatch latch = new CountDownLatch(1);

        StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
        client.stream(fimRequest, fimResponse -> {
            Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    @DisplayName("测试列出模型")
    public void testListModel(){
        ListModelResponse listModelResponse = client.get(EmptyRequest.createListModelRequest());
        String name = listModelResponse.getData().stream().map(ListModelResponse.Model::getId).collect(Collectors.joining(","));
        System.out.println(name);
        Assertions.assertNotNull(listModelResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试获取账户余额")
    public void testBalanceInfo(){
        BalanceInfoResponse balanceInfo = client.get(EmptyRequest.createBalanceRequest());
        Assertions.assertNotNull(balanceInfo,"balanceInfo不能为空");
    }
}
