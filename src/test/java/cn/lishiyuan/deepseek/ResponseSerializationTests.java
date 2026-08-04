package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.response.ResponseRequest;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.config.enums.ResponseReasoningEffortEnums;
import cn.lishiyuan.deepseek.config.enums.ResponseTextFormatEnums;
import cn.lishiyuan.deepseek.config.enums.ToolChoiceEnums;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Responses API 序列化与流式解析校验，不依赖网络与 accessKey。
 * 配置与 {@link DefaultClient} 中的 ObjectMapper 保持一致。
 */
@DisplayName("Responses API 测试")
public class ResponseSerializationTests {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private JsonNode serialize(ResponseRequest req) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(req));
    }

    private JsonNode serialize(ResponseStreamRequest req) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(req));
    }

    private ResponseRequest base(String input) {
        return ResponseRequest.create(input, ModelEnums.DEEPSEEK_V4_FLASH.code);
    }

    @Test
    @DisplayName("默认请求：model=deepseek-v4-flash，stream=false，无 path/responseClass 泄漏")
    public void testDefaults() throws Exception {
        JsonNode node = serialize(base("hi"));
        assertEquals("deepseek-v4-flash", node.get("model").asText());
        assertEquals(false, node.get("stream").asBoolean());
        assertEquals("hi", node.get("input").asText());
        assertNull(node.get("path"));
        assertNull(node.get("responseClass"));
        // reasoning 未设置应被省略（服务端按默认思考开启处理）
        assertNull(node.get("reasoning"));
    }

    @Test
    @DisplayName("input 字符串形态序列化为 JSON 字符串")
    public void testInputAsString() throws Exception {
        JsonNode node = serialize(base("hello"));
        assertTrue(node.get("input").isTextual(), "input 应为字符串");
        assertEquals("hello", node.get("input").asText());
    }

    @Test
    @DisplayName("input 数组形态序列化为输入项数组，字段映射正确")
    public void testInputAsArray() throws Exception {
        ResponseRequest.InputItem msg = new ResponseRequest.InputItem();
        msg.setRole("user");
        msg.setContent("hello");
        ResponseRequest.InputItem fc = new ResponseRequest.InputItem();
        fc.setType("function_call");
        fc.setCallId("call_1");
        fc.setName("get_weather");
        fc.setArguments("{\"city\":\"sz\"}");
        ResponseRequest.InputItem out = new ResponseRequest.InputItem();
        out.setType("function_call_output");
        out.setCallId("call_1");
        out.setOutput("{\"temp\":30}");

        ResponseRequest req = ResponseRequest.create(List.of(msg, fc, out), ModelEnums.DEEPSEEK_V4_FLASH.code);
        JsonNode node = serialize(req);

        assertTrue(node.get("input").isArray(), "input 应为数组");
        assertEquals(3, node.get("input").size());
        // message 项：role + content(字符串)
        JsonNode msgNode = node.get("input").get(0);
        assertEquals("user", msgNode.get("role").asText());
        assertTrue(msgNode.get("content").isTextual());
        assertEquals("hello", msgNode.get("content").asText());
        // function_call 项
        JsonNode fcNode = node.get("input").get(1);
        assertEquals("function_call", fcNode.get("type").asText());
        assertEquals("call_1", fcNode.get("call_id").asText());
        assertEquals("get_weather", fcNode.get("name").asText());
        assertEquals("{\"city\":\"sz\"}", fcNode.get("arguments").asText());
        // function_call_output 项
        JsonNode outNode = node.get("input").get(2);
        assertEquals("function_call_output", outNode.get("type").asText());
        assertEquals("call_1", outNode.get("call_id").asText());
        assertEquals("{\"temp\":30}", outNode.get("output").asText());
    }

    @Test
    @DisplayName("input 内容块数组形态")
    public void testInputContentBlocks() throws Exception {
        ResponseRequest.InputContentBlock block = new ResponseRequest.InputContentBlock();
        block.setType("input_text");
        block.setText("hello");
        ResponseRequest.InputItem msg = new ResponseRequest.InputItem();
        msg.setRole("user");
        msg.setContent(List.of(block));

        JsonNode node = serialize(ResponseRequest.create(List.of(msg), ModelEnums.DEEPSEEK_V4_FLASH.code));
        JsonNode content = node.get("input").get(0).get("content");
        assertTrue(content.isArray(), "content 应为数组");
        assertEquals("input_text", content.get(0).get("type").asText());
        assertEquals("hello", content.get(0).get("text").asText());
    }

    @Test
    @DisplayName("reasoning.effort 映射正确")
    public void testReasoningEffort() throws Exception {
        ResponseRequest req = base("hi");
        ResponseRequest.Reasoning r = new ResponseRequest.Reasoning();
        r.setEffort(ResponseReasoningEffortEnums.HIGH.code);
        req.setReasoning(r);
        JsonNode node = serialize(req);
        assertEquals("high", node.get("reasoning").get("effort").asText());
    }

    @Test
    @DisplayName("text.format json_schema：type/name/schema 映射")
    public void testTextFormatJsonSchema() throws Exception {
        ResponseRequest req = base("hi");
        ResponseRequest.Text text = new ResponseRequest.Text();
        ResponseRequest.TextFormat fmt = new ResponseRequest.TextFormat();
        fmt.setType(ResponseTextFormatEnums.JSON_SCHEMA.code);
        fmt.setName("weather");
        fmt.setSchema(Map.of(
                "type", "object",
                "properties", Map.of("temp", Map.of("type", "number")),
                "required", List.of("temp")
        ));
        text.setFormat(fmt);
        req.setText(text);

        JsonNode node = serialize(req);
        JsonNode fmtNode = node.get("text").get("format");
        assertEquals("json_schema", fmtNode.get("type").asText());
        assertEquals("weather", fmtNode.get("name").asText());
        assertEquals("object", fmtNode.get("schema").get("type").asText());
        assertEquals("number", fmtNode.get("schema").get("properties").get("temp").get("type").asText());
    }

    @Test
    @DisplayName("tools：function 与 web_search 两种类型")
    public void testTools() throws Exception {
        ResponseRequest req = base("hi");
        ResponseRequest.Tool fn = new ResponseRequest.Tool();
        fn.setName("get_weather");
        fn.setDescription("获取天气");
        fn.setParameters(Map.of("type", "object", "properties", Map.of()));
        ResponseRequest.Tool ws = new ResponseRequest.Tool();
        ws.setType("web_search");
        req.setTools(List.of(fn, ws));

        JsonNode node = serialize(req);
        assertEquals("function", node.get("tools").get(0).get("type").asText());
        assertEquals("get_weather", node.get("tools").get(0).get("name").asText());
        assertEquals("object", node.get("tools").get(0).get("parameters").get("type").asText());
        assertEquals("web_search", node.get("tools").get(1).get("type").asText());
        assertNull(node.get("tools").get(1).get("name"), "web_search 无 name 应省略");
    }

    @Test
    @DisplayName("tool_choice：字符串形式与对象形式")
    public void testToolChoice() throws Exception {
        ResponseRequest req = base("hi");
        req.setToolChoice(ToolChoiceEnums.AUTO);
        assertTrue(serialize(req).get("tool_choice").isTextual());
        assertEquals("auto", serialize(req).get("tool_choice").asText());

        ResponseRequest.ToolChoice tc = new ResponseRequest.ToolChoice();
        tc.setType("function");
        tc.setName("get_weather");
        req.setToolChoice(tc);
        JsonNode tcNode = serialize(req).get("tool_choice");
        assertTrue(tcNode.isObject());
        assertEquals("function", tcNode.get("type").asText());
        assertEquals("get_weather", tcNode.get("name").asText());
    }

    @Test
    @DisplayName("标量字段映射：instructions/max_output_tokens/temperature/top_p/top_logprobs/user")
    public void testScalarFields() throws Exception {
        ResponseRequest req = base("hi");
        req.setInstructions("be concise");
        req.setMaxOutputTokens(1024);
        req.setTemperature(0.5);
        req.setTopP(0.9);
        req.setTopLogprobs(5);
        req.setUser("user-123");

        JsonNode node = serialize(req);
        assertEquals("be concise", node.get("instructions").asText());
        assertEquals(1024, node.get("max_output_tokens").asInt());
        assertEquals(0.5, node.get("temperature").asDouble());
        assertEquals(0.9, node.get("top_p").asDouble());
        assertEquals(5, node.get("top_logprobs").asInt());
        assertEquals("user-123", node.get("user").asText());
    }

    @Test
    @DisplayName("StreamRequest：stream=true")
    public void testStreamRequest() throws Exception {
        ResponseStreamRequest req = ResponseStreamRequest.create("hi", ModelEnums.DEEPSEEK_V4_FLASH.code);
        JsonNode node = serialize(req);
        assertEquals(true, node.get("stream").asBoolean());
        assertEquals("deepseek-v4-flash", node.get("model").asText());
        assertNull(node.get("path"));
        assertNull(node.get("responseClass"));
    }

    // ==================== 流式解析（命名 SSE 事件） ====================

    private static final String SSE_SAMPLE = String.join("\n",
            "event: response.created",
            "data: {\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"status\":\"in_progress\",\"model\":\"deepseek-v4-flash\"}}",
            "",
            "event: response.output_item.added",
            "data: {\"sequence_number\":1,\"output_index\":0,\"item\":{\"type\":\"message\",\"id\":\"msg_1\",\"role\":\"assistant\",\"status\":\"in_progress\",\"content\":[]}}",
            "",
            "event: response.output_text.delta",
            "data: {\"sequence_number\":2,\"item_id\":\"msg_1\",\"output_index\":0,\"content_index\":0,\"delta\":\"Hello\"}",
            "",
            "event: response.output_text.delta",
            "data: {\"sequence_number\":3,\"item_id\":\"msg_1\",\"output_index\":0,\"content_index\":0,\"delta\":\" world\"}",
            "",
            "event: response.completed",
            "data: {\"response\":{\"id\":\"resp_1\",\"object\":\"response\",\"status\":\"completed\",\"model\":\"deepseek-v4-flash\",\"usage\":{\"input_tokens\":5,\"output_tokens\":2,\"total_tokens\":7}}}",
            "",
            "");

    private List<ResponseStreamEvent> pump(String sse) {
        Flux<ResponseStreamEvent> flux = Flux.create(sink -> {
            try {
                DefaultClient.pumpEventStream(new BufferedReader(new StringReader(sse)), sink);
                sink.complete();
            } catch (IOException e) {
                sink.error(e);
            }
        });
        return flux.collectList().block(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("命名事件解析：type 注入、字段反序列化、terminal 终止")
    public void testEventStreamParsing() {
        List<ResponseStreamEvent> events = pump(SSE_SAMPLE);
        assertEquals(5, events.size(), "应解析出 5 个事件");

        ResponseStreamEvent created = events.get(0);
        assertEquals("response.created", created.getType());
        assertEquals("resp_1", created.getResponse().getId());
        assertEquals("in_progress", created.getResponse().getStatus());
        assertEquals("response", created.getResponse().getObject());

        ResponseStreamEvent itemAdded = events.get(1);
        assertEquals("response.output_item.added", itemAdded.getType());
        assertEquals(1, itemAdded.getSequenceNumber());
        assertEquals(0, itemAdded.getOutputIndex());
        assertEquals("message", itemAdded.getItem().getType());
        assertEquals("assistant", itemAdded.getItem().getRole());

        ResponseStreamEvent delta1 = events.get(2);
        assertEquals("response.output_text.delta", delta1.getType());
        assertEquals("Hello", delta1.getDelta());
        assertEquals("msg_1", delta1.getItemId());
        assertEquals(0, delta1.getContentIndex());

        ResponseStreamEvent delta2 = events.get(3);
        assertEquals(" world", delta2.getDelta());

        ResponseStreamEvent completed = events.get(4);
        assertEquals("response.completed", completed.getType());
        assertEquals("completed", completed.getResponse().getStatus());
        assertEquals(7, completed.getResponse().getUsage().getTotalTokens());
        assertEquals(5, completed.getResponse().getUsage().getInputTokens());
    }

    @Test
    @DisplayName("terminal 事件后停止，不解析后续内容")
    public void testTerminalStopsParsing() {
        String sse = String.join("\n",
                "event: response.failed",
                "data: {\"response\":{\"id\":\"resp_1\",\"status\":\"failed\",\"error\":{\"code\":\"server_error\",\"message\":\"boom\"}}}",
                "",
                "event: response.output_text.delta",
                "data: {\"delta\":\"should not appear\"}",
                "",
                "");
        List<ResponseStreamEvent> events = pump(sse);
        assertEquals(1, events.size(), "failed 之后应停止，仅 1 个事件");
        assertEquals("response.failed", events.get(0).getType());
        assertEquals("server_error", events.get(0).getResponse().getError().getCode());
    }

    @Test
    @DisplayName("无 event 行时 type 为 null，data 仍可解析")
    public void testNoEventLine() {
        String sse = "data: {\"delta\":\"plain\"}\n\n";
        List<ResponseStreamEvent> events = pump(sse);
        assertEquals(1, events.size());
        assertNull(events.get(0).getType());
        assertEquals("plain", events.get(0).getDelta());
    }
}
