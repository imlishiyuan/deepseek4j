package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 会话标题开关（请求体 {@code title}，DeepSeek 扩展字段）。
 *
 * <p>传 {@code {"enabled": true}} 后，响应会带上由模型顺带生成的简短会话标题：
 * 非流式在 {@code message.title}，流式在最后一个 chunk 的 {@code delta.title}。
 * 标题不计费；回答结束后最多再等 5 秒，响应不一定带标题，请自行兜底。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/conversation_title">会话标题</a>
 */
@Data
public class TitleOptions {

    /** 是否返回会话标题。 */
    @JsonProperty("enabled")
    private Boolean enabled = true;

    /** 开启会话标题。 */
    public static TitleOptions enabled() {
        TitleOptions options = new TitleOptions();
        options.setEnabled(true);
        return options;
    }
}
