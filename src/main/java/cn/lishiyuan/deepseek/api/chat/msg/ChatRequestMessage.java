package cn.lishiyuan.deepseek.api.chat.msg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 请求消息基类：role（构造时由子类固定，不可变）+ content。
 * 各角色由独立子类表示，避免无关字段互相污染（如 user 消息设置 tool_calls、tool 消息设置 prefix）。
 *
 * @see NamedMessage system/user/assistant 共有 name 字段
 * @see ToolMessage 工具结果消息（无 name，带 tool_call_id）
 */
@Getter
@Setter
public abstract class ChatRequestMessage {
    /** 角色，由子类构造器固定 */
    @JsonProperty("role")
    private final String role;

    @JsonProperty("content")
    private String content;

    protected ChatRequestMessage(String role) {
        this.role = role;
    }
}
