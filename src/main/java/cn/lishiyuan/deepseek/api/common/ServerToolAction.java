package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 服务端工具调用的参数（`ServerToolCall.action`）。
 *
 * <p>{@code type} 取值：
 * <ul>
 *   <li>{@code search}：联网搜索，搜索词在 {@code queries}；</li>
 *   <li>{@code open_page}：打开网页（深度思考搜索），地址在 {@code url}；</li>
 *   <li>{@code find_in_page}：在已打开网页中查找，用到 {@code url} 与 {@code pattern}；</li>
 *   <li>{@code read_link}：阅读链接（deepseek_read_url），地址在 {@code url}。</li>
 * </ul>
 */
@Data
public class ServerToolAction {

    /** search / open_page / find_in_page / read_link */
    @JsonProperty("type")
    private String type;

    /** 搜索词；{@code type=search} 时返回。一次调用可包含多个查询词。 */
    @JsonProperty("queries")
    private List<String> queries;

    /** 目标网页地址；{@code open_page} / {@code find_in_page} / {@code read_link} 时返回。 */
    @JsonProperty("url")
    private String url;

    /** 页内查找的关键词；{@code find_in_page} 时返回。 */
    @JsonProperty("pattern")
    private String pattern;
}
