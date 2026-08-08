package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.chat.*;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.api.common.FuncParamDefinition;
import cn.lishiyuan.deepseek.config.enums.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 请求体序列化校验，不依赖网络与 accessKey。
 * 配置与 {@link DefaultClient} 中的 ObjectMapper 保持一致。
 */
@DisplayName("请求序列化测试")
public class RequestSerializationTests {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private JsonNode serialize(ChatRequest req) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(req));
    }

    private ChatRequest baseRequest() {
        UserMessage m = new UserMessage();
        m.setContent("hi");
        return ChatRequest.create(List.of(m), ModelEnums.DEEPSEEK_V4_FLASH.code);
    }

    @Test
    @DisplayName("默认请求：thinking.type=disabled，不含 reasoning_effort，不含 path/responseClass")
    public void testDefaults() throws Exception {
        JsonNode node = serialize(baseRequest());
        assertEquals("disabled", node.get("thinking").get("type").asText());
        assertNull(node.get("thinking").get("reasoning_effort"));
        assertNull(node.get("path"));
        assertNull(node.get("responseClass"));
        assertEquals("deepseek-v4-flash", node.get("model").asText());
        assertEquals(false, node.get("stream").asBoolean());
    }

    @Test
    @DisplayName("thinking.reasoning_effort 可设置")
    public void testReasoningEffort() throws Exception {
        ChatRequest req = baseRequest();
        req.getThinking().setType(ThinkingEnums.ENABLED.code);
        req.getThinking().setReasoningEffort(ThinkingEffortEnums.MAX.code);
        JsonNode node = serialize(req);
        assertEquals("enabled", node.get("thinking").get("type").asText());
        assertEquals("max", node.get("thinking").get("reasoning_effort").asText());
    }

    @Test
    @DisplayName("function.parameters 为 JSON Schema 对象（非数组），strict 可设置")
    public void testParametersIsObject() throws Exception {
        ChatRequest req = baseRequest();
        Tool tool = new Tool();
        ToolFunction fn = new ToolFunction();
        fn.setName("get_weather");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setType(FuncParamEnums.OBJ.code);
        FuncParamDefinition funcParamDefinition = new FuncParamDefinition();
        funcParamDefinition.setType(FuncParamEnums.STR.code);
        funcParamDefinition.setDescription("城市名称");
        functionParameter.setProperties(Map.of("city",funcParamDefinition));
        functionParameter.setRequired(List.of("city"));
        fn.setParameters(functionParameter);
        fn.setStrict(true);
        tool.setFunction(fn);
        req.setTools(List.of(tool));

        JsonNode node = serialize(req);
        JsonNode params = node.get("tools").get(0).get("function").get("parameters");
        assertTrue(params.isObject(), "parameters 应为 JSON 对象而非数组");
        assertEquals("object", params.get("type").asText());
        assertEquals("string", params.get("properties").get("city").get("type").asText());
        assertEquals("city", params.get("required").get(0).asText());
        assertEquals(true, node.get("tools").get(0).get("function").get("strict").asBoolean());
    }

    @Test
    @DisplayName("tool_choice 支持字符串形式（枚举直接传入与 .code 字符串）")
    public void testToolChoiceString() throws Exception {
        ChatRequest req = baseRequest();
        req.setToolChoice(ToolChoiceEnums.AUTO);
        JsonNode node = serialize(req);
        assertTrue(node.get("tool_choice").isTextual(), "枚举应序列化为字符串");
        assertEquals("auto", node.get("tool_choice").asText());

        req.setToolChoice(ToolChoiceEnums.REQUIRED.code);
        assertEquals("required", serialize(req).get("tool_choice").asText());
    }

    @Test
    @DisplayName("tool_choice 支持对象形式（指定 tool）")
    public void testToolChoiceObject() throws Exception {
        ChatRequest req = baseRequest();
        Tool tc = new Tool();
        ToolFunction fn = new ToolFunction();
        fn.setName("get_weather");
        tc.setFunction(fn);
        req.setToolChoice(tc);

        JsonNode node = serialize(req);
        assertTrue(node.get("tool_choice").isObject());
        assertEquals("function", node.get("tool_choice").get("type").asText());
        assertEquals("get_weather", node.get("tool_choice").get("function").get("name").asText());
    }

    @Test
    @DisplayName("user_id 字段映射正确")
    public void testUserId() throws Exception {
        ChatRequest req = baseRequest();
        req.setUserId("user-123");
        assertEquals("user-123", serialize(req).get("user_id").asText());
    }

    @Test
    @DisplayName("已废弃字段仍可序列化")
    @SuppressWarnings("deprecation")
    public void testDeprecatedFields() throws Exception {
        ChatRequest req = baseRequest();
        req.setFrequencyPenalty(0.5);
        req.setPresencePenalty(0.5);
        JsonNode node = serialize(req);
        assertEquals(0.5, node.get("frequency_penalty").asDouble());
        assertEquals(0.5, node.get("presence_penalty").asDouble());
    }
}
