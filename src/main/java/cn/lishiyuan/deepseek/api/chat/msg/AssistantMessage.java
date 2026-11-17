package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.api.chat.ServerToolStep;
import cn.lishiyuan.deepseek.api.chat.ToolCall;
import cn.lishiyuan.deepseek.config.enums.RoleEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

    /**
     * 服务端工具调用（DeepSeek 扩展）。多轮对话时必须随 assistant 消息<b>原样回传</b>：
     * 阅读链接的网页正文加密在 {@code signature} 里，丢弃后之前读过的网页不再进入上下文。
     */
    @JsonProperty("server_tool_calls")
    private List<ServerToolStep> serverToolCalls;

    public AssistantMessage() {
        super(RoleEnums.ASSISTANT.code);
    }
}
