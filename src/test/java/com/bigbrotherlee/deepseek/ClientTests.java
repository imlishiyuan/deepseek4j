package com.bigbrotherlee.deepseek;

import com.bigbrotherlee.deepseek.enums.ModelEnums;
import com.bigbrotherlee.deepseek.enums.RoleEnums;
import com.bigbrotherlee.deepseek.request.ChatRequest;
import com.bigbrotherlee.deepseek.request.FimRequest;
import com.bigbrotherlee.deepseek.response.BalanceInfoResponse;
import com.bigbrotherlee.deepseek.response.ChatResponse;
import com.bigbrotherlee.deepseek.response.FimResponse;
import com.bigbrotherlee.deepseek.response.ListModelResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        client.streamChat(chatRequest,chatResponse -> Assertions.assertNotNull(chatResponse,"listModelResponse不应该为空"));
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
    public void testStreamFIM(){
        FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
        client.streamFim(fimRequest, fimResponse -> Assertions.assertNotNull(fimResponse,"fimResponse不应该为空"));
    }

    @Test
    @DisplayName("测试列出模型")
    public void testListModel(){
        ListModelResponse listModelResponse = client.listModel();
        Assertions.assertNotNull(listModelResponse,"listModelResponse不应该为空");
    }

    @Test
    @DisplayName("测试获取账户余额")
    public void testBalanceInfo(){
        BalanceInfoResponse balanceInfo = client.getBalanceInfo();
        Assertions.assertNotNull(balanceInfo,"balanceInfo不能为空");
    }
}
