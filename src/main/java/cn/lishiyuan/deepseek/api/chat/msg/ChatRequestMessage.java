package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.api.chat.msg.content.ContentPart;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

/**
 * 请求消息基类：role（构造时由子类固定，不可变）+ content。
 * 各角色由独立子类表示，避免无关字段互相污染（如 user 消息设置 tool_calls、tool 消息设置 prefix）。
 *
 * <p>content 是多模态后的可空字段：
 * <ul>
 *   <li>纯文本：直接 {@link #setContent(String)} 传入字符串；</li>
 *   <li>多模态（文本 + 图片 / 文件）：{@link #setContent(List)} 传入内容块数组（{@code List<ContentPart>}）。</li>
 * </ul>
 *
 * @see NamedMessage system/user/assistant 共有 name 字段
 * @see ToolMessage 工具结果消息（无 name，带 tool_call_id）
 */
@Getter
public abstract class ChatRequestMessage {
    /** 角色，由子类构造器固定 */
    @JsonProperty("role")
    private final String role;

    /** 内容：纯文本 String 或 List<ContentPart>（多模态块数组）。 */
    @JsonProperty("content")
    private Object content;

    protected ChatRequestMessage(String role) {
        this.role = role;
    }

    /**
     * 设置纯文本内容。
     */
    public void setContent(String text) {
        this.content = text;
    }

    /**
     * 设置多模态内容块数组（text / image_url / file）。
     */
    public void setContent(List<ContentPart> parts) {
        this.content = parts;
    }

    /**
     * 设置多模态内容块数组（可变参数形式，{@code setContent(ContentPart.text("..."), ContentPart.imageUrl("url"))}）。
     */
    public void setContent(ContentPart... parts) {
        this.content = List.of(parts);
    }
}
