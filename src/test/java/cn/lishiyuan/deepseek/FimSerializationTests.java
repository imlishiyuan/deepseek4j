package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.fim.FimRequest;
import cn.lishiyuan.deepseek.api.fim.StreamFimRequest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FIM 请求体序列化校验，不依赖网络与 accessKey。
 * 配置与 {@link DefaultClient} 中的 ObjectMapper 保持一致。
 */
@DisplayName("FIM 请求序列化测试")
public class FimSerializationTests {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private JsonNode serialize(FimRequest req) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(req));
    }

    private JsonNode serialize(StreamFimRequest req) throws Exception {
        return MAPPER.readTree(MAPPER.writeValueAsString(req));
    }

    @Test
    @DisplayName("FimRequest 默认：stream=false，无顶层 include_usage，无 path/responseClass 泄漏")
    public void testFimDefaults() throws Exception {
        FimRequest req = FimRequest.create("def hello():", "deepseek-chat");
        JsonNode node = serialize(req);

        assertEquals("deepseek-chat", node.get("model").asText());
        assertEquals("def hello():", node.get("prompt").asText());
        assertEquals(false, node.get("stream").asBoolean());
        // 顶层不应出现 include_usage（仅 stream_options.include_usage 合法）
        assertNull(node.get("include_usage"), "顶层 include_usage 为无效字段，不应出现");
        // 抽象基类字段不应泄漏
        assertNull(node.get("path"));
        assertNull(node.get("responseClass"));
    }

    @Test
    @DisplayName("StreamFimRequest：stream=true，无顶层 include_usage")
    public void testStreamFimDefaults() throws Exception {
        StreamFimRequest req = StreamFimRequest.create("def hello():", "deepseek-chat");
        JsonNode node = serialize(req);

        assertEquals(true, node.get("stream").asBoolean());
        assertNull(node.get("include_usage"), "顶层 include_usage 为无效字段，不应出现");
        assertNull(node.get("path"));
        assertNull(node.get("responseClass"));
    }

    @Test
    @DisplayName("stream_options.include_usage 映射正确，且不产生顶层 include_usage")
    public void testStreamOptionsIncludeUsage() throws Exception {
        FimRequest req = FimRequest.create("x", "deepseek-chat");
        FimRequest.StreamOptions opts = new FimRequest.StreamOptions();
        opts.setIncludeUsage(true);
        req.setStreamOptions(opts);

        JsonNode node = serialize(req);
        assertTrue(node.has("stream_options"), "stream_options 应存在");
        assertEquals(true, node.get("stream_options").get("include_usage").asBoolean());
        assertNull(node.get("include_usage"), "顶层不应出现 include_usage");
    }

    @Test
    @DisplayName("FIM 各字段映射：echo/suffix/temperature/top_p/max_tokens/stop/logprobs")
    public void testFimFieldMapping() throws Exception {
        FimRequest req = FimRequest.create("def hello():", "deepseek-chat");
        req.setEcho(true);
        req.setSuffix("    return");
        req.setTemperature(0.5);
        req.setTopP(0.9);
        req.setMaxTokens(100);
        req.setStop(List.of("\n\n"));
        req.setLogprobs(1);

        JsonNode node = serialize(req);
        assertEquals(true, node.get("echo").asBoolean());
        assertEquals("    return", node.get("suffix").asText());
        assertEquals(0.5, node.get("temperature").asDouble());
        assertEquals(0.9, node.get("top_p").asDouble());
        assertEquals(100, node.get("max_tokens").asInt());
        assertEquals("\n\n", node.get("stop").get(0).asText());
        assertEquals(1, node.get("logprobs").asInt());
    }

    @Test
    @DisplayName("已废弃字段 frequency_penalty/presence_penalty 仍可序列化")
    @SuppressWarnings("deprecation")
    public void testDeprecatedFields() throws Exception {
        FimRequest req = FimRequest.create("x", "deepseek-chat");
        req.setFrequencyPenalty(0.5);
        req.setPresencePenalty(0.5);
        JsonNode node = serialize(req);
        assertEquals(0.5, node.get("frequency_penalty").asDouble());
        assertEquals(0.5, node.get("presence_penalty").asDouble());
    }

    @Test
    @DisplayName("空 stop 列表与未设置字段不产生空节点")
    public void testNullOmission() throws Exception {
        FimRequest req = FimRequest.create("x", "deepseek-chat");
        JsonNode node = serialize(req);
        assertFalse(node.has("echo"), "未设置的 echo 应被 NON_NULL 省略");
        assertFalse(node.has("stop"), "未设置的 stop 应被省略");
        assertFalse(node.has("stream_options"), "未设置的 stream_options 应被省略");
    }
}
