package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.agent.Calculator;
import cn.lishiyuan.deepseek.agent.DefaultToolsCaller;
import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.agent.WebTools;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.Message;
import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.api.chat.ToolCallFunction;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.config.AgentConfig;
import cn.lishiyuan.deepseek.config.enums.SendTimeEnums;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认工具（{@link WebTools} / {@link Calculator}）的测试。
 *
 * <p>分两部分：
 * <ul>
 *   <li><b>离线</b>：工具注册与参数 schema、Calculator 的纯函数行为（含精度参数 scale），
 *       以及经 {@link DefaultToolsCaller} 的端到端调用（schema → 参数解析 → 方法调用 → 结果序列化）；</li>
 *   <li><b>集成</b>：通过 {@link Agent} 让模型真实调用这些工具（需要 accessKey 与网络，Search/Fetch 按次计费）。</li>
 * </ul>
 */
@DisplayName("默认工具测试：WebTools + Calculator")
public class ToolsTests {

    /** 单次 agent 回复最长等待时间（一轮里可能包含多次模型请求 + 工具调用） */
    private static final Duration TIMEOUT = Duration.ofMinutes(3);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 离线使用的工具集合。WebTools 的 client 传 null：
     * 离线部分只用于生成 schema 与调用 Calculator，不会真正发起网络请求。
     */
    private static final ToolsCaller TOOLS =
            new DefaultToolsCaller(List.of(new Calculator(), new WebTools(null)));

    private static final Map<String, Tool> TOOL_MAP = TOOLS.tools().stream()
            .collect(Collectors.toMap(tool -> tool.getFunction().getName(), Function.identity()));

    private static final Calculator CALCULATOR = new Calculator();

    private static Agent agent;

    @BeforeAll
    public static void initEnv() {
        // read ackey
        Path path = Paths.get(ToolsTests.class.getClassLoader().getResource("accessKey.txt").getPath());
        try {
            String accessKey = Files.readString(path);
            Client client = new DefaultClient(accessKey);

            AgentConfig agentConfig = new AgentConfig("tools-test");
            agentConfig.setName("cn.lishiyuan.deepseek.tools");
            agentConfig.setSystemPrompt("""
                    你是一个工具使用助手。
                    需要做精确的十进制计算时，必须调用 calculator_* 工具（默认保留两位小数）。
                    需要联网信息时，必须调用 web_search / web_fetch 工具。
                    回答时直接给出结论，不要虚构工具未返回的信息。""");
            agentConfig.setToolsCaller(new DefaultToolsCaller(List.of(new Calculator(), new WebTools(client))));
            agent = new DefaultAgent(client, agentConfig);

        } catch (IOException e) {
            throw new RuntimeException("读取accessKey失败", e);
        }
    }

    // ── 测试辅助 ────────────────────────────────────────────────────────────

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

    /** 直接调用工具（不经过模型），并把工具返回的 JSON 结果转成字符串。 */
    private static String callTool(String name, String argumentsJson) throws Exception {
        ToolCallFunction function = new ToolCallFunction();
        function.setName(name);
        function.setArguments(argumentsJson);
        ToolCall toolCall = new ToolCall();
        toolCall.setId("call_" + name);
        toolCall.setType("function");
        toolCall.setFunction(function);
        return MAPPER.readTree(TOOLS.call(toolCall)).asText();
    }

    // ── 离线：工具注册与 schema ─────────────────────────────────────────────

    @Test
    @DisplayName("工具注册：WebTools 与 Calculator 的 @DefTool 方法都被注册")
    public void testToolRegistration() {
        Assertions.assertEquals(21, TOOLS.tools().size(), "应为 2 个网络工具 + 19 个计算工具");
        Assertions.assertTrue(TOOL_MAP.keySet().containsAll(List.of(
                // 网络
                "web_search", "web_fetch",
                // 四则
                "calculator_plus", "calculator_minus", "calculator_multiply", "calculator_divide",
                // 幂与开方
                "calculator_square", "calculator_sqrt", "calculator_cbrt", "calculator_power",
                // 三角函数
                "calculator_sin", "calculator_cos", "calculator_tan",
                // 反三角函数
                "calculator_asin", "calculator_acos", "calculator_atan",
                // 其他
                "calculator_abs", "calculator_ln", "calculator_log10", "calculator_exp", "calculator_mod")),
                "实际注册的工具：" + TOOL_MAP.keySet());

        TOOL_MAP.values().forEach(tool -> {
            Assertions.assertEquals("function", tool.getType());
            Assertions.assertNotNull(tool.getFunction().getName(), "工具名不应为空");
            Assertions.assertNotNull(tool.getFunction().getDescription(), "工具描述不应为空");
        });
    }

    @Test
    @DisplayName("工具 schema：Calculator 参数类型；所有计算工具统一带 scale 参数")
    public void testToolSchema() {
        // 二元计算工具：left / right 两个 string 参数 + scale
        var plusParams = TOOL_MAP.get("calculator_plus").getFunction().getParameters();
        Assertions.assertNotNull(plusParams, "calculator_plus 应有参数定义");
        Assertions.assertEquals("string", plusParams.getProperties().get("left").getType());
        Assertions.assertEquals("string", plusParams.getProperties().get("right").getType());
        Assertions.assertEquals("integer", plusParams.getProperties().get("scale").getType());
        Assertions.assertEquals(List.of("left", "right", "scale"), plusParams.getRequired());

        // 幂运算：base / exponent / scale
        var powerParams = TOOL_MAP.get("calculator_power").getFunction().getParameters();
        Assertions.assertEquals("string", powerParams.getProperties().get("base").getType());
        Assertions.assertEquals("string", powerParams.getProperties().get("exponent").getType());
        Assertions.assertEquals("integer", powerParams.getProperties().get("scale").getType());

        // 三角函数：x / unit / scale
        var sinParams = TOOL_MAP.get("calculator_sin").getFunction().getParameters();
        Assertions.assertEquals("string", sinParams.getProperties().get("x").getType());
        Assertions.assertEquals("string", sinParams.getProperties().get("unit").getType());
        Assertions.assertEquals("integer", sinParams.getProperties().get("scale").getType());

        // 一元计算工具：x / scale
        var sqrtParams = TOOL_MAP.get("calculator_sqrt").getFunction().getParameters();
        Assertions.assertEquals("string", sqrtParams.getProperties().get("x").getType());
        Assertions.assertEquals("integer", sqrtParams.getProperties().get("scale").getType());
        Assertions.assertEquals(List.of("x", "scale"), sqrtParams.getRequired());

        // 统一性：所有 calculator_* 工具都必须带 scale 参数
        TOOL_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("calculator_"))
                .forEach(entry -> Assertions.assertNotNull(
                        entry.getValue().getFunction().getParameters().getProperties().get("scale"),
                        entry.getKey() + " 缺少 scale 参数"));

        // web_fetch：单个 url 参数
        var fetchParams = TOOL_MAP.get("web_fetch").getFunction().getParameters();
        Assertions.assertEquals("string", fetchParams.getProperties().get("url").getType());

        // web_search：参数是 SearchCondition（record），应展开为 object 并带上各组件属性
        var searchParams = TOOL_MAP.get("web_search").getFunction().getParameters();
        var condition = searchParams.getProperties().get("condition");
        Assertions.assertNotNull(condition, "web_search 应有 condition 参数");
        Assertions.assertEquals("object", condition.getType());
        Assertions.assertEquals("string", condition.getProperties().get("query").getType());
        Assertions.assertEquals("integer", condition.getProperties().get("count").getType());
        Assertions.assertEquals("boolean", condition.getProperties().get("needContent").getType());
        Assertions.assertEquals("string", condition.getProperties().get("localization").getType());
        Assertions.assertEquals("string", condition.getProperties().get("oriQuestion").getType());
    }

    // ── 离线：Calculator 四则运算 ───────────────────────────────────────────

    @Test
    @DisplayName("Calculator 加法：默认两位小数并四舍五入")
    public void testPlus() {
        Assertions.assertEquals("2124.69", CALCULATOR.plus("1234.567", "890.123", null));
        Assertions.assertEquals("0.30", CALCULATOR.plus("0.1", "0.2", null));
        // 1.005 -> 两位小数 HALF_UP -> 1.01
        Assertions.assertEquals("1.01", CALCULATOR.plus("1.005", "0", null));
        Assertions.assertEquals("0.00", CALCULATOR.plus("1.001", "-1.001", null));
    }

    @Test
    @DisplayName("Calculator 减法：默认两位小数并四舍五入")
    public void testMinus() {
        Assertions.assertEquals("10.00", CALCULATOR.minus("10.005", "0.005", null));
        Assertions.assertEquals("-10.00", CALCULATOR.minus("5", "15", null));
    }

    @Test
    @DisplayName("Calculator 乘法：默认两位小数并四舍五入")
    public void testMultiply() {
        Assertions.assertEquals("10.00", CALCULATOR.multiply("2.5", "4", null));
        // 1.005 * 0.5 = 0.5025 -> 0.50
        Assertions.assertEquals("0.50", CALCULATOR.multiply("1.005", "0.5", null));
    }

    @Test
    @DisplayName("Calculator 除法：默认两位小数四舍五入，无限小数不抛异常")
    public void testDivide() {
        Assertions.assertEquals("3.33", CALCULATOR.divide("10", "3", null));
        // 1 / 8 = 0.125 -> 0.13
        Assertions.assertEquals("0.13", CALCULATOR.divide("1", "8", null));
    }

    @Test
    @DisplayName("Calculator 除零：返回可读错误而不是抛异常")
    public void testDivideByZero() {
        String result = CALCULATOR.divide("1", "0", null);
        Assertions.assertTrue(result.startsWith("计算失败"), "除零应返回计算失败说明，实际：" + result);
        Assertions.assertTrue(result.contains("除数不能为 0"), "错误说明应提到除数为 0，实际：" + result);
    }

    // ── 离线：幂与开方 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Calculator 平方：x 的 2 次方")
    public void testSquare() {
        Assertions.assertEquals("6.25", CALCULATOR.square("2.5", null));
        Assertions.assertEquals("0.01", CALCULATOR.square("0.1", null));
        Assertions.assertEquals("9.00", CALCULATOR.square("-3", null));
    }

    @Test
    @DisplayName("Calculator 平方根：负数返回可读错误")
    public void testSqrt() {
        Assertions.assertEquals("1.41", CALCULATOR.sqrt("2", null));
        Assertions.assertEquals("2.00", CALCULATOR.sqrt("4", null));
        Assertions.assertEquals("0.50", CALCULATOR.sqrt("0.25", null));
        Assertions.assertEquals("0.00", CALCULATOR.sqrt("0", null));

        String negative = CALCULATOR.sqrt("-1", null);
        Assertions.assertTrue(negative.startsWith("计算失败"), "负数开平方应返回计算失败说明，实际：" + negative);
    }

    @Test
    @DisplayName("Calculator 立方根：支持负数")
    public void testCbrt() {
        Assertions.assertEquals("3.00", CALCULATOR.cbrt("27", null));
        Assertions.assertEquals("-2.00", CALCULATOR.cbrt("-8", null));
    }

    @Test
    @DisplayName("Calculator 幂运算：整数指数与小数指数；非实数结果返回错误")
    public void testPower() {
        Assertions.assertEquals("1024.00", CALCULATOR.power("2", "10", null));
        // 2 的 0.5 次方 = 根号 2
        Assertions.assertEquals("1.41", CALCULATOR.power("2", "0.5", null));
        Assertions.assertEquals("3.00", CALCULATOR.power("9", "0.5", null));

        String notReal = CALCULATOR.power("-2", "0.5", null);
        Assertions.assertTrue(notReal.startsWith("计算失败"), "负底数开平方应返回计算失败说明，实际：" + notReal);
    }

    // ── 离线：精度参数 scale ────────────────────────────────────────────────

    @Test
    @DisplayName("精度参数：默认 2 位，可指定 0~10 位")
    public void testScale() {
        // 开方 / 幂：精度到位后可以拿到更多有效数字
        Assertions.assertEquals("1.414214", CALCULATOR.sqrt("2", 6));
        Assertions.assertEquals("1.4142135624", CALCULATOR.sqrt("2", 10));
        Assertions.assertEquals("1", CALCULATOR.sqrt("2", 0));
        Assertions.assertEquals("1.414214", CALCULATOR.power("2", "0.5", 6));

        // 除法：按 scale 直接截断/进位
        Assertions.assertEquals("3.333333", CALCULATOR.divide("10", "3", 6));
        Assertions.assertEquals("3", CALCULATOR.divide("10", "3", 0));

        // 三角函数 / 对数 / 指数
        Assertions.assertEquals("0.500000", CALCULATOR.sin("30", "deg", 6));
        Assertions.assertEquals("2.718282", CALCULATOR.exp("1", 6));
        Assertions.assertEquals("0.693147", CALCULATOR.ln("2", 6));

        // 四则运算同样受 scale 控制
        Assertions.assertEquals("1", CALCULATOR.plus("1.005", "0", 0));
        Assertions.assertEquals("1.005", CALCULATOR.plus("1.005", "0", 3));

        // scale 越界：返回可读错误
        String tooLarge = CALCULATOR.sqrt("2", 11);
        Assertions.assertTrue(tooLarge.startsWith("计算失败"), "scale 超过上限应返回计算失败说明，实际：" + tooLarge);
        String negative = CALCULATOR.sqrt("2", -1);
        Assertions.assertTrue(negative.startsWith("计算失败"), "scale 为负数应返回计算失败说明，实际：" + negative);
    }

    // ── 离线：三角函数与反三角函数 ──────────────────────────────────────────

    @Test
    @DisplayName("Calculator 三角函数：默认角度制，也支持弧度制")
    public void testTrigonometric() {
        Assertions.assertEquals("0.50", CALCULATOR.sin("30", "deg", null));
        Assertions.assertEquals("0.00", CALCULATOR.sin("0", "deg", null));
        Assertions.assertEquals("1.00", CALCULATOR.sin("90", "deg", null));
        Assertions.assertEquals("0.50", CALCULATOR.cos("60", "deg", null));
        Assertions.assertEquals("1.00", CALCULATOR.cos("0", "deg", null));
        Assertions.assertEquals("1.00", CALCULATOR.tan("45", "deg", null));
        Assertions.assertEquals("0.00", CALCULATOR.tan("0", "deg", null));

        // 弧度制：π/2 的正弦为 1
        Assertions.assertEquals("1.00", CALCULATOR.sin("1.5707963267948966", "rad", null));
        // unit 不传时按「度」处理
        Assertions.assertEquals("0.50", CALCULATOR.sin("30", null, null));
    }

    @Test
    @DisplayName("Calculator 反三角函数：结果单位可指定，入参超出定义域返回错误")
    public void testInverseTrigonometric() {
        Assertions.assertEquals("90.00", CALCULATOR.asin("1", "deg", null));
        Assertions.assertEquals("30.00", CALCULATOR.asin("0.5", "deg", null));
        Assertions.assertEquals("60.00", CALCULATOR.acos("0.5", "deg", null));
        Assertions.assertEquals("90.00", CALCULATOR.acos("0", "deg", null));
        Assertions.assertEquals("45.00", CALCULATOR.atan("1", "deg", null));

        // 弧度制：asin(0.5) = π/6 ≈ 0.52
        Assertions.assertEquals("0.52", CALCULATOR.asin("0.5", "rad", null));

        String outOfDomain = CALCULATOR.asin("2", "deg", null);
        Assertions.assertTrue(outOfDomain.startsWith("计算失败"),
                "asin 入参超出 [-1,1] 应返回计算失败说明，实际：" + outOfDomain);
    }

    // ── 离线：其他常用运算 ──────────────────────────────────────────────────

    @Test
    @DisplayName("Calculator 绝对值 / 对数 / 指数 / 取余")
    public void testOtherMath() {
        Assertions.assertEquals("3.50", CALCULATOR.abs("-3.5", null));
        Assertions.assertEquals("2.00", CALCULATOR.abs("2", null));

        Assertions.assertEquals("0.00", CALCULATOR.ln("1", null));
        Assertions.assertEquals("1.00", CALCULATOR.ln("2.718281828459045", null));
        Assertions.assertEquals("3.00", CALCULATOR.log10("1000", null));
        Assertions.assertEquals("0.00", CALCULATOR.log10("1", null));

        Assertions.assertEquals("1.00", CALCULATOR.exp("0", null));
        Assertions.assertEquals("2.72", CALCULATOR.exp("1", null));

        Assertions.assertEquals("1.00", CALCULATOR.mod("10", "3", null));
        Assertions.assertEquals("-1.00", CALCULATOR.mod("-10", "3", null));

        String modByZero = CALCULATOR.mod("10", "0", null);
        Assertions.assertTrue(modByZero.contains("除数不能为 0"), "取余除数为 0 应返回错误说明，实际：" + modByZero);

        String lnOfZero = CALCULATOR.ln("0", null);
        Assertions.assertTrue(lnOfZero.startsWith("计算失败"), "ln(0) 应返回计算失败说明，实际：" + lnOfZero);
        String lnOfNegative = CALCULATOR.ln("-1", null);
        Assertions.assertTrue(lnOfNegative.startsWith("计算失败"), "ln(负数) 应返回计算失败说明，实际：" + lnOfNegative);
    }

    @Test
    @DisplayName("Calculator 非法入参：空值与非法数字都返回可读错误")
    public void testInvalidOperand() {
        Assertions.assertTrue(CALCULATOR.plus(null, "1", null).contains("操作数不能为空"));
        Assertions.assertTrue(CALCULATOR.plus("  ", "1", null).contains("操作数不能为空"));
        Assertions.assertTrue(CALCULATOR.plus("abc", "1", null).startsWith("计算失败"));
        Assertions.assertTrue(CALCULATOR.sqrt("abc", null).startsWith("计算失败"));
    }

    // ── 离线：经 DefaultToolsCaller 的端到端调用 ────────────────────────────

    @Test
    @DisplayName("工具调用链路：calculator_* 参数解析（含 scale）与结果序列化")
    public void testCallCalculatorTool() throws Exception {
        // 不传 scale 时按默认 2 位
        Assertions.assertEquals("2124.69",
                callTool("calculator_plus", "{\"left\":\"1234.567\",\"right\":\"890.123\"}"));
        Assertions.assertEquals("0.13",
                callTool("calculator_divide", "{\"left\":\"1\",\"right\":\"8\"}"));
        // 模型把数字写成 JSON number（而不是字符串）也应能解析
        Assertions.assertEquals("10.00",
                callTool("calculator_multiply", "{\"left\":2.5,\"right\":4}"));
        // 新增的幂 / 根 / 三角工具同样走通链路
        Assertions.assertEquals("6.25", callTool("calculator_square", "{\"x\":\"2.5\"}"));
        Assertions.assertEquals("1.41", callTool("calculator_sqrt", "{\"x\":\"2\"}"));
        Assertions.assertEquals("1024.00",
                callTool("calculator_power", "{\"base\":\"2\",\"exponent\":\"10\"}"));
        Assertions.assertEquals("0.50",
                callTool("calculator_sin", "{\"x\":\"30\",\"unit\":\"deg\"}"));
        Assertions.assertEquals("30.00",
                callTool("calculator_asin", "{\"x\":\"0.5\",\"unit\":\"deg\"}"));
        // 显式传 scale（JSON number）
        Assertions.assertEquals("1.414214", callTool("calculator_sqrt", "{\"x\":\"2\",\"scale\":6}"));
        Assertions.assertEquals("0.5000",
                callTool("calculator_sin", "{\"x\":\"30\",\"unit\":\"deg\",\"scale\":4}"));
        // 除零走工具的错误分支
        Assertions.assertTrue(callTool("calculator_divide", "{\"left\":\"1\",\"right\":\"0\"}")
                .contains("除数不能为 0"));
    }

    // ── 集成：agent 真实调用工具 ────────────────────────────────────────────

    @Test
    @DisplayName("集成：agent 调用 Calculator 做精确计算")
    public void testAgentUseCalculator() {
        ChatResponse response = assertReply("计算器", agent.send(
                message("请用 calculator_divide 工具计算 1 除以 8，并告诉我它按2位小数四舍五入后的结果。"),
                SendTimeEnums.NEXT_TURN).block(TIMEOUT));

        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println("计算器回复：" + content);
        // 工具结果是 0.13（1/8 = 0.125 四舍五入到两位小数），模型直接手算会得到 0.125
        Assertions.assertTrue(content.contains("0.13"),
                "应体现计算器工具的结果 0.13，实际回复：" + content);
    }

    @Test
    @DisplayName("集成：agent 调用三角函数/开方等扩展工具")
    public void testAgentUseAdvancedCalculator() {
        ChatResponse response = assertReply("扩展计算器", agent.send(
                message("请用 calculator_sqrt 工具计算 2 的平方根，并告诉我保留 6 位小数后的结果。"),
                SendTimeEnums.NEXT_TURN).block(TIMEOUT));

        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println("扩展计算器回复：" + content);
        Assertions.assertTrue(content.contains("1.414214"),
                "应体现开方工具（scale=6）的结果 1.414214，实际回复：" + content);
    }

    @Test
    @DisplayName("集成：agent 调用 web_search 搜索")
    public void testAgentUseWebSearch() {
        ChatResponse response = assertReply("web_search", agent.send(
                message("请用 web_search 工具搜索「DeepSeek 官网」，然后告诉我 DeepSeek 的官方网站域名。"),
                SendTimeEnums.NEXT_TURN).block(TIMEOUT));

        String content = response.getChoices().get(0).getMessage().getContent().toLowerCase(Locale.ROOT);
        System.out.println("搜索回复：" + content);
        Assertions.assertTrue(content.contains("deepseek.com"),
                "搜索结果应指向 deepseek.com，实际回复：" + content);
    }

    @Test
    @DisplayName("集成：agent 调用 web_fetch 抓取网页")
    public void testAgentUseWebFetch() {
        ChatResponse response = assertReply("web_fetch", agent.send(
                message("请用 web_fetch 工具抓取 https://example.com 这个网页，然后告诉我它的标题。"),
                SendTimeEnums.NEXT_TURN).block(TIMEOUT));

        String content = response.getChoices().get(0).getMessage().getContent().toLowerCase(Locale.ROOT);
        System.out.println("抓取回复：" + content);
        // example.com 的页面标题是 "Example Domain"
        Assertions.assertTrue(content.contains("example"),
                "抓取 example.com 后回复应提到 Example，实际回复：" + content);
    }
}
