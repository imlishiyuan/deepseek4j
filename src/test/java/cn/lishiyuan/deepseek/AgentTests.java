package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.agent.DefaultToolsCaller;
import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.Message;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.config.AgentConfig;
import cn.lishiyuan.deepseek.config.enums.SendTimeEnums;
import cn.lishiyuan.deepseek.tools.MapTools;
import cn.lishiyuan.deepseek.tools.TripTools;
import cn.lishiyuan.deepseek.tools.WeatherTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@DisplayName("DefaultAgent agent loop 测试")
public class AgentTests {

    /** 单个响应最长等待时间（一轮里可能包含多次模型请求 + 工具调用） */
    private static final Duration TIMEOUT = Duration.ofMinutes(3);

    private static Agent agent;

    @BeforeAll
    public static void initEnv(){
        // read ackey
        Path path = Paths.get(ClientTests.class.getClassLoader().getResource("accessKey.txt").getPath());
        try {
            String accessKey = Files.readString(path);
            Client client = new DefaultClient(accessKey);

            AgentConfig agentConfig = new AgentConfig("1");
            agentConfig.setName("com.lishiyuan.deepseek.agent");
            agentConfig.setSystemPrompt("你是由李干嘛构建的智能体（agent）。");
            List<Object> tools = List.of(new MapTools(), new TripTools(), new WeatherTools());
            ToolsCaller defaultToolsCaller = new DefaultToolsCaller(tools);
            agentConfig.setToolsCaller(defaultToolsCaller);
            agent = new DefaultAgent(client,agentConfig);

        } catch (IOException e) {
            throw new RuntimeException("读取accessKey失败",e);
        }
    }

    private static UserMessage message(String content) {
        UserMessage userMessage = new UserMessage();
        userMessage.setContent(content);
        return userMessage;
    }

    /** 断言一次调用拿到了非空回复，并返回响应供进一步断言。 */
    private static ChatResponse assertReply(String label, ChatResponse response) {
        Assertions.assertNotNull(response, "响应不应为空：" + label);
        Assertions.assertNotNull(response.getChoices(), "choices 不应为空：" + label);
        Assertions.assertFalse(response.getChoices().isEmpty(), "choices 不应为空：" + label);
        Message message = response.getChoices().get(0).getMessage();
        Assertions.assertNotNull(message, "message 不应为空：" + label);
        Assertions.assertNotNull(message.getContent(), "content 不应为空：" + label);
        Assertions.assertFalse(message.getContent().isBlank(), "content 不应为空白：" + label);
        return response;
    }

    private static ChatResponse sendAndAssert(String label, String content, SendTimeEnums sendTime) {
        return assertReply(label, agent.send(message(content), sendTime).block(TIMEOUT));
    }

    @Test
    @DisplayName("测试agent能力（带断言回归）")
    public void testAgent(){

        // 前两条按 NEXT_REQUEST 连续投递（会并入同一轮的下一次请求），第三条 NEXT_TURN 开启下一轮
        Mono<ChatResponse> weather = agent.send(
                message("我准备国庆七天去新疆阿勒泰旅游帮我看看天气情况？"), SendTimeEnums.NEXT_REQUEST);
        Mono<ChatResponse> spots = agent.send(
                message("帮我顺便一下阿勒泰著名景点和美食"), SendTimeEnums.NEXT_REQUEST);

        assertReply("天气", weather.block(TIMEOUT));
        assertReply("景点和美食", spots.block(TIMEOUT));

        ChatResponse booking = sendAndAssert("订机票",
                "如果天气好的话帮我订一张2号北京出发的机票，随便那张都行，我是李干嘛。",
                SendTimeEnums.NEXT_TURN);
        System.out.println("订票回复：" + booking.getChoices().get(0).getMessage().getContent());
    }

    @Test
    @DisplayName("回归：history 跨轮次延续（下一轮能看到上一轮说过的话）")
    public void testHistoryAcrossTurns(){
        // 用系统提示里没有的信息，避免靠 system prompt 蒙对
        sendAndAssert("记住订单号", "请记住：我的订单号是 AB-7788。", SendTimeEnums.NEXT_TURN);

        ChatResponse recall = sendAndAssert("回忆订单号", "我的订单号是多少？", SendTimeEnums.NEXT_TURN);
        String content = recall.getChoices().get(0).getMessage().getContent();
        Assertions.assertTrue(content.contains("AB-7788"),
                "下一轮应能记起上一轮给出的订单号，实际回复：" + content);
    }

}
