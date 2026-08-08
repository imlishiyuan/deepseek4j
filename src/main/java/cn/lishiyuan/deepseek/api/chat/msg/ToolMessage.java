package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.config.enums.RoleEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * tool 角色消息：工具调用结果，需带 tool_call_id 关联上一步 assistant 的 tool_calls。
 */
@Getter
@Setter
public class ToolMessage extends ChatRequestMessage {
    @JsonProperty("tool_call_id")
    private String toolCallId;

    public ToolMessage() {
        super(RoleEnums.TOOL.code);
    }
}
