package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.enums.ModelEnums;
import cn.lishiyuan.deepseek.request.ChatRequest;
import cn.lishiyuan.deepseek.request.FimRequest;
import cn.lishiyuan.deepseek.response.ChatResponse;
import cn.lishiyuan.deepseek.response.FimResponse;
import cn.lishiyuan.deepseek.response.ListModelResponse;
import cn.lishiyuan.deepseek.enums.RoleEnums;
import cn.lishiyuan.deepseek.response.BalanceInfoResponse;
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
        ChatRequest.Message systemMessage = new ChatRequest.Message();
        systemMessage.setRole(RoleEnums.SYSTEM.code);
        systemMessage.setContent("You are a helpful assistant");

        ChatRequest.Message userMessage = new ChatRequest.Message();
        userMessage.setRole(RoleEnums.USER.code);
        userMessage.setContent("你好");

        List<ChatRequest.Message> messageList = List.of(systemMessage, userMessage);
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
        ChatResponse chatResponse = client.chat(chatRequest);
        Assertions.assertNotNull(chatResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试流对话")
    public void testStreamChat(){
        ChatRequest.Message systemMessage = new ChatRequest.Message();
        systemMessage.setRole(RoleEnums.SYSTEM.code);
        systemMessage.setContent("You are a helpful assistant");

        ChatRequest.Message userMessage = new ChatRequest.Message();
        userMessage.setRole(RoleEnums.USER.code);
        userMessage.setContent("你好");

        List<ChatRequest.Message> messageList = List.of(systemMessage, userMessage);
        ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
        CountDownLatch latch = new CountDownLatch(1);

        client.streamChat(chatRequest,chatResponse -> {
            Assertions.assertNotNull(chatResponse,"listModelResponse不应该为空");
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
        FimResponse fimResponse = client.fim(fimRequest);
        Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
    }

    @Test
    @DisplayName("测试流FIM")
    public void testStreamFIM() {
        CountDownLatch latch = new CountDownLatch(1);

        FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
        client.streamFim(fimRequest, fimResponse -> {
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
        ListModelResponse listModelResponse = client.listModel();
        String name = listModelResponse.getData().stream().map(ListModelResponse.Model::getId).collect(Collectors.joining(","));
        System.out.println(name);
        Assertions.assertNotNull(listModelResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试获取账户余额")
    public void testBalanceInfo(){
        BalanceInfoResponse balanceInfo = client.getBalanceInfo();
        Assertions.assertNotNull(balanceInfo,"balanceInfo不能为空");
    }
}
