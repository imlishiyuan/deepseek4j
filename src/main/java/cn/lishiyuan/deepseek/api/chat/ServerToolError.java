package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 服务端工具调用失败时的错误信息（{@link ServerToolCall#getError()}）。
 *
 * <p>常见 {@code code}：
 * <ul>
 *   <li>{@code url_not_accessible}：无法读取该链接；</li>
 *   <li>{@code url_not_allowed}：不允许读取，或网页未通过模型内容安全审核；</li>
 *   <li>{@code unavailable}：服务不可用、深度思考搜索中打开网页失败，或调用未完成。</li>
 * </ul>
 */
@Data
public class ServerToolError {

    @JsonProperty("code")
    private String code;
}
