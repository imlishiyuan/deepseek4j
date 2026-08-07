package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 带 name 字段的消息基类：system/user/assistant 共有 name，tool 消息无 name。
 */
@Getter
@Setter
public abstract class NamedMessage extends ChatRequestMessage {
    @JsonProperty("name")
    private String name;

    protected NamedMessage(String role) {
        super(role);
    }
}
