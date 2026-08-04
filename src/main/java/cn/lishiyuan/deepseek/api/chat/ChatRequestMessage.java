package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRequestMessage {
    @JsonProperty("role")
    private String role;
    @JsonProperty("content")
    private String content;
    @JsonProperty("name")
    private String name;
    // assistant消息
    @JsonProperty("prefix")
    private Boolean prefix;
    // assistant消息
    @JsonProperty("reasoning_content")
    private String reasoningContent;
    // tool消息
    @JsonProperty("tool_call_id")
    private String toolCallId;
}
