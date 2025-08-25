package cn.lishiyuan.deepseek.api.chat;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChatRequestMessage {
    @JSONField(name = "role")
    private String role;
    @JSONField(name = "content")
    private String content;
    @JSONField(name = "name")
    private String name;
    // assistant消息
    @JSONField(name = "prefix")
    private Boolean prefix;
    // assistant消息
    @JSONField(name = "reasoning_content")
    private String reasoningContent;
    // tool消息
    @JSONField(name = "tool_call_id")
    private String toolCallId;
}
