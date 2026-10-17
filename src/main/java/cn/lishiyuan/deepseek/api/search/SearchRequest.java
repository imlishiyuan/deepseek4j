package cn.lishiyuan.deepseek.api.search;

import cn.lishiyuan.deepseek.api.BaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 网页搜索请求（POST /v1/tools/search）。
 * <p>按 query 搜索网页，返回结构化结果列表。由 DeepSeek 自研搜索引擎提供支持。
 *
 * <p>{@code query} 支持把搜索语法原样传给搜索服务，可组合使用：
 * <ul>
 *   <li>{@code site:} / {@code -site:}：限定 / 排除站点；</li>
 *   <li>{@code -} / {@code -"..."}：排除关键词 / 短语；</li>
 *   <li>{@code "..."}：精确匹配短语；</li>
 *   <li>{@code lang:}：限定语言（逗号分隔）；</li>
 *   <li>{@code after:} / {@code before:}：按发布日期限定。</li>
 * </ul>
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/search">网页搜索与抓取</a>
 */
@Data
public class SearchRequest extends BaseRequest<SearchResponse> {

    /** 搜索词，1～128 个 Unicode 字符，不能全为空白。支持上述搜索语法。 */
    @JsonProperty("query")
    private String query;

    /** 返回条数，1～50；不传由服务端按 10 处理。 */
    @JsonProperty("count")
    private Integer count;

    /** 触发搜索的用户原始问题，1～128 个字符（可空）；有助于理解搜索意图，传 null 视为未提供。 */
    @JsonProperty("ori_question")
    private String oriQuestion;

    /** 用于优先返回本地化搜索结果的 IP 地址，默认空字符串。 */
    @JsonProperty("localization")
    private String localization;

    /** 是否在 {@code content} 中返回网页正文，默认 false；为 false 时 content 恒为空字符串。 */
    @JsonProperty("need_content")
    private Boolean needContent;

    public static SearchRequest create(String query) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        return request;
    }

    @Override
    public Class<SearchResponse> getResponseClass() {
        return SearchResponse.class;
    }

    @Override
    public String getPath() {
        return "v1/tools/search";
    }
}
