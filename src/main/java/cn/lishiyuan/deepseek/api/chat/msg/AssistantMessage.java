package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.config.enums.RoleEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * assistant 角色消息：可带前缀补全、推理内容、模型发起的工具调用。
 */
@Getter
@Setter
public class AssistantMessage extends NamedMessage {
    /** 前缀补全 */
    @JsonProperty("prefix")
    private Boolean prefix;
    /** 模型推理内容 */
    @JsonProperty("reasoning_content")
    private String reasoningContent;
    /**
     * 模型发起的工具调用。多轮工具调用时，回传 assistant 消息必须带上，
     * 以匹配后续 tool 消息的 tool_call_id；否则 tool 消息成为孤儿，API 返回 400。
     */
    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    public AssistantMessage() {
        super(RoleEnums.ASSISTANT.code);
    }
}
