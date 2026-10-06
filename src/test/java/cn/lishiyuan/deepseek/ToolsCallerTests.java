package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.agent.DefaultToolsCaller;
import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.api.chat.FunctionParameter;
import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.api.chat.ToolCallFunction;
import cn.lishiyuan.deepseek.config.enums.FuncParamEnums;
import cn.lishiyuan.deepseek.tools.MapTools;
import cn.lishiyuan.deepseek.tools.TripTools;
import cn.lishiyuan.deepseek.tools.WeatherTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 测试工具（{@code @DefTool} / {@code @DefParam}）的注册与调用，纯离线、不依赖网络与 accessKey。
 * <p>用静态字段初始化，而不是 {@code @BeforeAll}（本仓库的 surefire 版本不执行静态生命周期方法）。
 */
@DisplayName("测试工具注册与调用")
public class ToolsCallerTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ToolsCaller TOOLS_CALLER =
            new DefaultToolsCaller(List.of(new MapTools(), new TripTools(), new WeatherTools()));

    private static final Map<String, Tool> TOOL_MAP = TOOLS_CALLER.tools().stream()
            .collect(Collectors.toMap(tool -> tool.getFunction().getName(), Function.identity()));

    private static ToolCall call(String name, String argumentsJson) {
        ToolCallFunction function = new ToolCallFunction();
        function.setName(name);
        function.setArguments(argumentsJson);
        ToolCall toolCall = new ToolCall();
        toolCall.setId("call_" + name);
        toolCall.setType("function");
        toolCall.setFunction(function);
        return toolCall;
    }

    private static JsonNode invoke(String name, String argumentsJson) throws Exception {
        return MAPPER.readTree(TOOLS_CALLER.call(call(name, argumentsJson)));
    }

    // ── 注册 / schema ────────────────────────────────────────────────────────

    @Test
    @DisplayName("三个工具类的全部 @DefTool 方法都被注册")
    public void testToolsRegistered() {
        assertEquals(7, TOOLS_CALLER.tools().size());
        assertTrue(TOOL_MAP.keySet().containsAll(List.of(
                "get_weather", "get_weather_forecast",
                "search_scenic_spots", "search_local_food", "plan_route",
                "search_flights", "book_flight")));
        TOOLS_CALLER.tools().forEach(tool -> {
            assertEquals("function", tool.getType());
            assertNotNull(tool.getFunction().getName());
            assertNotNull(tool.getFunction().getDescription());
        });
    }

    @Test
    @DisplayName("参数 schema：多参数工具生成 object + 类型 + required（按声明顺序）")
    public void testParameterSchema() {
        FunctionParameter parameters = TOOL_MAP.get("get_weather_forecast").getFunction().getParameters();
        assertNotNull(parameters);
        assertEquals(FuncParamEnums.OBJ.code, parameters.getType());
        assertEquals(FuncParamEnums.STR.code, parameters.getProperties().get("city").getType());
        assertEquals(FuncParamEnums.INT.code, parameters.getProperties().get("days").getType());
        assertEquals(List.of("city", "days"), parameters.getRequired());
    }

    @Test
    @DisplayName("record 参数 schema：展开为 object，且组件上的 @DefParam 说明生效")
    public void testRecordParamSchema() {
        FunctionParameter parameters = TOOL_MAP.get("book_flight").getFunction().getParameters();
        assertEquals(List.of("request"), parameters.getRequired());

        FunctionParameter request = parameters.getProperties().get("request");
        assertEquals(FuncParamEnums.OBJ.code, request.getType());
        assertEquals("预订信息", request.getDescription());
        assertEquals(List.of("passenger", "flightNo", "date"), request.getRequired());
        assertEquals("乘客姓名，例如：李干嘛", request.getProperties().get("passenger").getDescription());
        assertEquals(FuncParamEnums.STR.code, request.getProperties().get("passenger").getType());
    }

    // ── 调用 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("WeatherTools.getWeather：String 参数")
    public void testGetWeather() throws Exception {
        JsonNode json = invoke("get_weather", "{\"city\":\"阿勒泰\"}");
        assertEquals("阿勒泰", json.get("city").asText());
        assertEquals(18, json.get("temperatureC").asInt());
    }

    @Test
    @DisplayName("WeatherTools.getWeatherForecast：String + Integer 参数")
    public void testGetWeatherForecast() throws Exception {
        JsonNode json = invoke("get_weather_forecast", "{\"city\":\"阿勒泰\",\"days\":5}");
        assertTrue(json.isArray());
        assertEquals(5, json.size());
        assertEquals("第 1 天", json.get(0).get("day").asText());
    }

    @Test
    @DisplayName("MapTools：景点 / 美食 / 路线（路线参数为 record）")
    public void testMapTools() throws Exception {
        JsonNode spots = invoke("search_scenic_spots", "{\"city\":\"阿勒泰\"}");
        assertEquals(3, spots.size());
        assertEquals("喀纳斯景区", spots.get(0).get("name").asText());

        JsonNode foods = invoke("search_local_food", "{\"city\":\"阿勒泰\"}");
        assertEquals(4, foods.size());
        assertEquals("烤全羊", foods.get(0).get("name").asText());

        JsonNode route = invoke("plan_route",
                "{\"request\":{\"from\":\"阿勒泰市区\",\"to\":\"喀纳斯景区\",\"mode\":\"walking\"}}");
        assertEquals("walking", route.get("mode").asText());
        assertEquals(3.2, route.get("distanceKm").asDouble());
        assertEquals(42, route.get("durationMinutes").asInt());
    }

    @Test
    @DisplayName("TripTools：航班查询与预订（参数均为 record）")
    public void testTripTools() throws Exception {
        JsonNode flights = invoke("search_flights",
                "{\"query\":{\"from\":\"西安\",\"to\":\"阿勒泰\",\"date\":\"2026-10-01\"}}");
        assertEquals(3, flights.size());
        assertEquals("CZ6886", flights.get(0).get("flightNo").asText());
        assertEquals("西安", flights.get(0).get("from").asText());

        JsonNode booking = invoke("book_flight",
                "{\"request\":{\"passenger\":\"李干嘛\",\"flightNo\":\"CZ6886\",\"date\":\"2026-10-01\"}}");
        assertEquals(true, booking.get("success").asBoolean());
        assertEquals("李干嘛", booking.get("passenger").asText());
        assertTrue(booking.get("orderId").asText().startsWith("ORD"));
    }

    // ── 边界 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("未知工具名返回提示，不抛异常")
    public void testUnknownTool() {
        String result = TOOLS_CALLER.call(call("no_such_tool", "{}"));
        assertFalse(result.isEmpty());
        assertEquals("tool doesn't exist", result);
    }

    @Test
    @DisplayName("多传 schema 之外的参数被忽略，不影响正常调用")
    public void testUnknownArgumentIgnored() throws Exception {
        JsonNode json = invoke("get_weather", "{\"city\":\"阿勒泰\",\"unexpected\":\"x\"}");
        assertEquals("阿勒泰", json.get("city").asText());
    }
}
