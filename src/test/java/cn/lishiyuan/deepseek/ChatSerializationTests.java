package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.StreamChatResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Chat 响应反序列化校验，重点验证共享 {@code Choice} 通过 {@code @JsonAlias("delta")}
 * 同时承载非流式（{@code message}）与流式（{@code delta}）两种 JSON 键。
 * 不依赖网络与 accessKey。配置与 {@link DefaultClient} 中的 ObjectMapper 保持一致。
 */
@DisplayName("Chat 响应反序列化测试")
public class ChatSerializationTests {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    @Test
    @DisplayName("非流式响应：choices[].message 反序列化到 Choice.message")
    public void testDeserializeNonStream() throws Exception {
        String json = "{\"id\":\"chatcmpl_1\",\"object\":\"chat.completion\","
                + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hi\"},"
                + "\"finish_reason\":\"stop\"}]}";
        ChatResponse resp = MAPPER.readValue(json, ChatResponse.class);
        assertEquals("chatcmpl_1", resp.getId());
        assertEquals(1, resp.getChoices().size());
        assertEquals("assistant", resp.getChoices().get(0).getMessage().getRole());
        assertEquals("hi", resp.getChoices().get(0).getMessage().getContent());
        assertEquals("stop", resp.getChoices().get(0).getFinishReason());
    }

    @Test
    @DisplayName("流式分片：choices[].delta 反序列化到 Choice.message（@JsonAlias 生效）")
    public void testDeserializeStreamDelta() throws Exception {
        String json = "{\"id\":\"chatcmpl_1\",\"object\":\"chat.completion.chunk\","
                + "\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"hel\"},"
                + "\"finish_reason\":null}]}";
        StreamChatResponse resp = MAPPER.readValue(json, StreamChatResponse.class);
        assertEquals("chat.completion.chunk", resp.getObject());
        assertEquals("assistant", resp.getChoices().get(0).getMessage().getRole());
        assertEquals("hel", resp.getChoices().get(0).getMessage().getContent());
        // 流式首帧常见：delta 仅有 role，content 缺省
        assertNull(resp.getChoices().get(0).getFinishReason());
    }

    @Test
    @DisplayName("流式分片缺省 content：delta 仅有 role 时 content 为 null")
    public void testDeserializeStreamDeltaRoleOnly() throws Exception {
        String json = "{\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"}}]}";
        StreamChatResponse resp = MAPPER.readValue(json, StreamChatResponse.class);
        assertEquals("assistant", resp.getChoices().get(0).getMessage().getRole());
        assertNull(resp.getChoices().get(0).getMessage().getContent());
    }

    @Test
    @DisplayName("usage 含 completion_tokens_details.reasoning_tokens 反序列化（共享 common.Usage）")
    public void testDeserializeUsage() throws Exception {
        String json = "{\"choices\":[],\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":5,"
                + "\"total_tokens\":15,\"completion_tokens_details\":{\"reasoning_tokens\":3}}}";
        ChatResponse resp = MAPPER.readValue(json, ChatResponse.class);
        assertEquals(10, resp.getUsage().getPromptTokens());
        assertEquals(15, resp.getUsage().getTotalTokens());
        assertEquals(3, resp.getUsage().getCompletionTokensDetails().getReasoningTokens());
    }
}
