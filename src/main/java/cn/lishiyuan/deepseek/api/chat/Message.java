package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Message {
    @JsonProperty("content")
    private String content;

    @JsonProperty("reasoning_content")
    private String reasoningContent;

    @JsonProperty("role")
    private String role;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;
}
