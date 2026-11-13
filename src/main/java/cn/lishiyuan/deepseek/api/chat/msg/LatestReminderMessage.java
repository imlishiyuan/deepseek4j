package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.config.enums.RoleEnums;

/**
 * 用户元信息标记消息（{@code role = "latest_reminder"}，DeepSeek 扩展角色）。
 *
 * <p>把当前日期、用户所在地、平台和语言告诉模型，让回答更贴合用户环境。
 * {@code content} 格式为 {@code <date>,<weekday>,<location>,<platform>,<language>}，例如
 * {@code 2026-11-11,星期三,新加坡,Web,中文}：
 * <ul>
 *   <li>{@code date}：{@code YYYY-MM-DD}；</li>
 *   <li>{@code weekday}：当天星期几，用 {@code language} 对应的语言书写；</li>
 *   <li>{@code location}：可选，可以为空，可以包含逗号；</li>
 *   <li>{@code platform}：{@code App} 或 {@code Web}；</li>
 *   <li>{@code language}：用该语言自身书写的语言名。</li>
 * </ul>
 *
 * <p>约束：必须紧挨在它所属的 {@code user} 消息之前；{}content 必须是字符串（否则 422），
 * 格式不符返回 400。支持 Chat Completions 与 Messages API，Responses API 不支持。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/latest_reminder">用户元信息标记</a>
 */
public class LatestReminderMessage extends ChatRequestMessage {

    public LatestReminderMessage() {
        super(RoleEnums.LATEST_REMINDER.code);
    }

    public LatestReminderMessage(String content) {
        this();
        setContent(content);
    }
}
