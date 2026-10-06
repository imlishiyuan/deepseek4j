package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.agent.DefaultToolsCaller;
import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.api.chat.FunctionParameter;
import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.api.chat.ToolCallFunction;
import cn.lishiyuan.deepseek.config.enums.FuncParamEnums;
import cn.lishiyuan.deepseek.tools.TypeTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultToolsCaller} 参数解析的全类型覆盖测试（离线）。
 * <p>方法名必须以 {@code test} 开头：本仓库的 surefire 2.12.4 只识别 {@code test*} 方法，
 * 且不执行 JUnit5 的 {@code @BeforeAll}/{@code @BeforeEach}/{@code @Disabled} 等生命周期注解。
 */
@DisplayName("工具参数解析（全类型）")
public class ToolsParamParsingTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ToolsCaller TOOLS = new DefaultToolsCaller(List.of(new TypeTools()));

    private static final Map<String, Tool> TOOL_MAP = TOOLS.tools().stream()
            .collect(Collectors.toMap(tool -> tool.getFunction().getName(), Function.identity()));

    private static JsonNode invoke(String name, String argumentsJson) throws Exception {
        ToolCallFunction function = new ToolCallFunction();
        function.setName(name);
        function.setArguments(argumentsJson);
        ToolCall toolCall = new ToolCall();
        toolCall.setId("call_" + name);
        toolCall.setType("function");
        toolCall.setFunction(function);
        return MAPPER.readTree(TOOLS.call(toolCall));
    }

    // ── 标量 ────────────────────────────────────────────────────────────────

    @Test
    public void testStringParam() throws Exception {
        assertEquals("hello", invoke("echo_string", "{\"v\":\"hello\"}").asText());
    }

    @Test
    public void testCharParam() throws Exception {
        assertEquals("A", invoke("echo_char", "{\"v\":\"A\"}").asText());
    }

    @Test
    public void testIntParam() throws Exception {
        assertEquals(42, invoke("echo_int", "{\"v\":42}").asInt());
        // 模型把数字写成字符串也要能解析
        assertEquals(42, invoke("echo_int", "{\"v\":\"42\"}").asInt());
    }

    @Test
    public void testIntegerParam() throws Exception {
        assertEquals(7, invoke("echo_integer", "{\"v\":7}").asInt());
    }

    @Test
    public void testLongParam() throws Exception {
        assertEquals(9007199254740993L, invoke("echo_long", "{\"v\":9007199254740993}").asLong());
    }

    @Test
    public void testDoubleParam() throws Exception {
        assertEquals(1.5, invoke("echo_double", "{\"v\":1.5}").asDouble(), 0.000001);
    }

    @Test
    public void testFloatParam() throws Exception {
        assertEquals(2.5f, invoke("echo_float", "{\"v\":2.5}").floatValue(), 0.000001f);
    }

    @Test
    public void testBooleanParam() throws Exception {
        assertTrue(invoke("echo_boolean", "{\"v\":true}").asBoolean());
    }

    @Test
    public void testBigDecimalParam() throws Exception {
        assertEquals(12.5, invoke("echo_bigdecimal", "{\"v\":12.5}").asDouble(), 0.000001);
    }

    @Test
    public void testBigIntegerParam() throws Exception {
        assertEquals("123456789012345678901234567890",
                invoke("echo_biginteger", "{\"v\":123456789012345678901234567890}").asText());
    }

    @Test
    public void testEnumParam() throws Exception {
        assertEquals("GREEN", invoke("echo_enum", "{\"v\":\"GREEN\"}").asText());
    }

    // ── 数组 / 集合 / Map ───────────────────────────────────────────────────

    @Test
    public void testIntArrayParam() throws Exception {
        JsonNode json = invoke("echo_int_array", "{\"v\":[1,2,3]}");
        assertEquals(3, json.size());
        assertEquals(2, json.get(1).asInt());
    }

    @Test
    public void testStringArrayParam() throws Exception {
        JsonNode json = invoke("echo_string_array", "{\"v\":[\"a\",\"b\"]}");
        assertEquals(2, json.size());
        assertEquals("b", json.get(1).asText());
    }

    @Test
    public void testStringListParam() throws Exception {
        JsonNode json = invoke("echo_string_list", "{\"v\":[\"x\",\"y\",\"z\"]}");
        assertEquals(3, json.size());
        assertEquals("z", json.get(2).asText());
    }

    @Test
    public void testPojoListParam() throws Exception {
        JsonNode json = invoke("echo_pojo_list", "{\"v\":[{\"x\":1,\"y\":2},{\"x\":3,\"y\":4}]}");
        assertEquals(2, json.size());
        assertEquals(1, json.get(0).get("x").asInt());
        assertEquals(4, json.get(1).get("y").asInt());
    }

    @Test
    public void testSetParam() throws Exception {
        JsonNode json = invoke("echo_set", "{\"v\":[\"a\",\"b\",\"c\"]}");
        assertEquals(3, json.size());
    }

    @Test
    public void testMapParam() throws Exception {
        JsonNode json = invoke("echo_map", "{\"v\":{\"a\":1,\"b\":2}}");
        assertEquals(1, json.get("a").asInt());
        assertEquals(2, json.get("b").asInt());
    }

    // ── POJO / 嵌套 / 时间 ──────────────────────────────────────────────────

    @Test
    public void testPojoParam() throws Exception {
        JsonNode json = invoke("echo_pojo", "{\"v\":{\"x\":1,\"y\":2}}");
        assertEquals(1, json.get("x").asInt());
        assertEquals(2, json.get("y").asInt());
    }

    @Test
    public void testNestedPojoParam() throws Exception {
        JsonNode json = invoke("echo_nested_pojo", "{\"v\":{\"name\":\"s\",\"center\":{\"x\":9,\"y\":8}}}");
        assertEquals("s", json.get("name").asText());
        assertEquals(9, json.get("center").get("x").asInt());
        assertEquals(8, json.get("center").get("y").asInt());
    }

    @Test
    public void testDateParam() throws Exception {
        // Date 默认按毫秒时间戳序列化
        assertEquals(1735689600000L, invoke("echo_date", "{\"v\":1735689600000}").asLong());
    }

    // ── 模型把对象/数组「字符串化」传进来 ────────────────────────────────────

    @Test
    public void testStringifiedPojoParam() throws Exception {
        JsonNode json = invoke("echo_pojo", "{\"v\":\"{\\\"x\\\":1,\\\"y\\\":2}\"}");
        assertEquals(1, json.get("x").asInt());
        assertEquals(2, json.get("y").asInt());
    }

    @Test
    public void testStringifiedArrayParam() throws Exception {
        JsonNode json = invoke("echo_int_array", "{\"v\":\"[1,2,3]\"}");
        assertEquals(3, json.size());
        assertEquals(3, json.get(2).asInt());
    }

    // ── 边界 ────────────────────────────────────────────────────────────────

    @Test
    public void testMissingParamBecomesNull() throws Exception {
        JsonNode json = invoke("echo_two", "{\"a\":\"x\"}");
        assertEquals("x", json.get(0).asText());
        assertEquals("null", json.get(1).asText());
    }

    @Test
    public void testNullArguments() throws Exception {
        assertTrue(invoke("echo_string", null).isNull());
    }

    @Test
    public void testEmptyArguments() throws Exception {
        assertTrue(invoke("echo_string", "{}").isNull());
        assertEquals("ok", invoke("no_arg", "{}").asText());
        assertEquals("ok", invoke("no_arg", null).asText());
    }

    @Test
    public void testExtraParamIgnored() throws Exception {
        assertEquals("x", invoke("echo_string", "{\"v\":\"x\",\"unexpected\":1}").asText());
    }

    @Test
    public void testUnknownTool() {
        ToolCallFunction function = new ToolCallFunction();
        function.setName("no_such_tool");
        function.setArguments("{}");
        ToolCall toolCall = new ToolCall();
        toolCall.setType("function");
        toolCall.setFunction(function);
        assertEquals("tool doesn't exist", TOOLS.call(toolCall));
    }

    @Test
    public void testPojoParamSchema() {
        // 顶层 properties 的键是「参数名 v」，POJO 字段在它下面一层
        FunctionParameter v = TOOL_MAP.get("echo_pojo").getFunction()
                .getParameters().getProperties().get("v");
        assertNotNull(v);
        assertEquals(FuncParamEnums.OBJ.code, v.getType());
        assertNotNull(v.getProperties().get("x"));
        assertNotNull(v.getProperties().get("y"));
        assertEquals(FuncParamEnums.INT.code, v.getProperties().get("x").getType());
        // 伪属性 class 必须被过滤掉（否则 Class -> class -> ... 无限递归）
        assertTrue(v.getProperties().get("class") == null);
    }

    @Test
    public void testNestedPojoParamSchema() {
        FunctionParameter v = TOOL_MAP.get("echo_nested_pojo").getFunction()
                .getParameters().getProperties().get("v");
        assertNotNull(v);
        FunctionParameter center = v.getProperties().get("center");
        assertNotNull(center);
        assertEquals(FuncParamEnums.OBJ.code, center.getType());
        assertNotNull(center.getProperties().get("x"));
    }

    // ── schema（Jackson 版）─────────────────────────────────────────────────

    private static FunctionParameter param(String toolName, String paramName) {
        return TOOL_MAP.get(toolName).getFunction().getParameters().getProperties().get(paramName);
    }

    private static String paramType(String toolName) {
        return param(toolName, "v").getType();
    }

    @Test
    public void testScalarSchemas() {
        assertEquals(FuncParamEnums.STR.code, paramType("echo_string"));
        assertEquals(FuncParamEnums.STR.code, paramType("echo_char"));
        assertEquals(FuncParamEnums.INT.code, paramType("echo_int"));
        assertEquals(FuncParamEnums.INT.code, paramType("echo_integer"));
        assertEquals(FuncParamEnums.INT.code, paramType("echo_long"));
        assertEquals(FuncParamEnums.NUM.code, paramType("echo_double"));
        assertEquals(FuncParamEnums.NUM.code, paramType("echo_float"));
        assertEquals(FuncParamEnums.BOOL.code, paramType("echo_boolean"));
        assertEquals(FuncParamEnums.NUM.code, paramType("echo_bigdecimal"));
        assertEquals(FuncParamEnums.INT.code, paramType("echo_biginteger"));
        assertEquals(FuncParamEnums.STR.code, paramType("echo_enum"));
    }

    @Test
    public void testArrayAndCollectionItemsSchema() {
        // 数组 / 集合要能补出 items（元素类型）
        assertEquals(FuncParamEnums.ARR.code, paramType("echo_int_array"));
        assertEquals(FuncParamEnums.INT.code, param("echo_int_array", "v").getItems().getType());
        assertEquals(FuncParamEnums.STR.code, param("echo_string_array", "v").getItems().getType());
        assertEquals(FuncParamEnums.STR.code, param("echo_string_list", "v").getItems().getType());
        assertEquals(FuncParamEnums.ARR.code, paramType("echo_set"));
        // List<POJO> 的 items 是对象，且能继续展开
        assertEquals(FuncParamEnums.OBJ.code, param("echo_pojo_list", "v").getItems().getType());
        assertNotNull(param("echo_pojo_list", "v").getItems().getProperties().get("x"));
    }

    @Test
    public void testNestedFieldDescriptions() {
        // 嵌套属性的名字/说明来自字段上的 @DefParam
        FunctionParameter point = param("echo_pojo", "v");
        assertEquals("横坐标", point.getProperties().get("x").getDescription());
        assertEquals("纵坐标", point.getProperties().get("y").getDescription());
        assertEquals(FuncParamEnums.INT.code, point.getProperties().get("x").getType());

        FunctionParameter shape = param("echo_nested_pojo", "v");
        assertEquals("中心点", shape.getProperties().get("center").getDescription());
        assertEquals("横坐标",
                shape.getProperties().get("center").getProperties().get("x").getDescription());
    }

    @Test
    public void testTopLevelParamDescriptionAndRequired() {
        FunctionParameter parameters = TOOL_MAP.get("echo_string").getFunction().getParameters();
        assertEquals("字符串", parameters.getProperties().get("v").getDescription());
        assertEquals(List.of("v"), parameters.getRequired());
    }

    @Test
    public void testMapSchemaIsObject() {
        // Map 目前只生成 object：FunctionParameter.additionalProperties 是 Boolean，表达不了「带类型的值」
        assertEquals(FuncParamEnums.OBJ.code, paramType("echo_map"));
    }

    @Test
    public void testRecordParamSchema() {
        // record 属性名来自组件名，BeanUtils 版认不出来，Jackson 版可以
        FunctionParameter v = param("echo_record", "v");
        assertEquals(FuncParamEnums.OBJ.code, v.getType());
        assertNotNull(v.getProperties().get("lat"));
        assertNotNull(v.getProperties().get("lng"));
        assertEquals(FuncParamEnums.NUM.code, v.getProperties().get("lat").getType());
        assertEquals(List.of("lat", "lng"), v.getRequired());
    }

    @Test
    public void testRecordParamCall() throws Exception {
        JsonNode json = invoke("echo_record", "{\"v\":{\"lat\":1.5,\"lng\":2.5}}");
        assertEquals(1.5, json.get("lat").asDouble(), 0.000001);
        assertEquals(2.5, json.get("lng").asDouble(), 0.000001);
    }
}
