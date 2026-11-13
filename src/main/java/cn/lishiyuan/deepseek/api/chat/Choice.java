package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * chat completion 的一个选项。
 * <p>同时承载非流式（JSON 键 {@code message}）与流式（JSON 键 {@code delta}）两种形态：
 * 字段 {@code message} 通过 {@link JsonAlias} 同时接受 {@code delta} 反序列化。
 * 此类仅被反序列化，SDK 不序列化它。
 */
@Data
public class Choice {
    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("message")
    @JsonAlias("delta")
    private Message message;

    @JsonProperty("logprobs")
    private Logprobs logprobs;

    /**
     * 流式响应中的服务端工具事件（DeepSeek 扩展字段，与 {@code delta} 并列）。
     * <p>一个 chunk 最多带一个事件；完整 {@code server_tool_calls} 与 {@code annotations}
     * 只在带 {@code finish_reason} 的最后一个 chunk 里给一次。
     */
    @JsonProperty("server_tool_event")
    private ServerToolEvent serverToolEvent;
}
