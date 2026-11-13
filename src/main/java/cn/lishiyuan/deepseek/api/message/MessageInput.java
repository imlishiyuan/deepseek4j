package cn.lishiyuan.deepseek.api.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Messages API 的一条对话消息（{@code role} + {@code content}）。
 *
 * <p>{@code role} 可以是 {@code user} / {@code assistant} / {@code latest_reminder}（DeepSeek 扩展）。
 * {@code content} 可以是字符串，也可以是内容块数组（{@code List<MessageContentBlock>}）。
 */
@Data
public class MessageInput {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private Object content;

    public MessageInput() {
    }

    public MessageInput(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public MessageInput(String role, Object content) {
        this.role = role;
        this.content = content;
    }

    public static MessageInput user(String text) {
        return new MessageInput("user", text);
    }

    public static MessageInput user(MessageContentBlock... blocks) {
        return new MessageInput("user", java.util.List.of(blocks));
    }

    public static MessageInput assistant(String text) {
        return new MessageInput("assistant", text);
    }

    public static MessageInput latestReminder(String content) {
        return new MessageInput("latest_reminder", content);
    }
}
