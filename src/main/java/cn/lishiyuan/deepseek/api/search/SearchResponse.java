package cn.lishiyuan.deepseek.api.search;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 网页搜索响应。
 * <p>没有匹配结果时 {@code results} 为空数组（这是正常结果，不是错误）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/search">网页搜索 API</a>
 */
@Data
public class SearchResponse extends BaseResponse {

    /** 搜索结果；没有匹配结果时为空数组。 */
    @JsonProperty("results")
    private List<Result> results;

    /** 单条搜索结果。 */
    @Data
    public static class Result {
        /**
         * 排序分数，0～3，综合考虑相关性、权威性、时效性等维度；大于等于 1.5 分为优质结果。
         */
        @JsonProperty("rerank_score")
        private Double rerankScore;

        /** 标题。 */
        @JsonProperty("title")
        private String title;

        /** 结果链接。 */
        @JsonProperty("url")
        private String url;

        /** 短摘要（通常 100～200 字符），与 query 相关，适合前端展现。 */
        @JsonProperty("snippet")
        private String snippet;

        /** 长摘要（通常 600～800 字符），与 query 相关，适合喂给模型阅读。 */
        @JsonProperty("summary")
        private String summary;

        /** 网页正文；{@code need_content} 为 false 或正文缺失时为空字符串。 */
        @JsonProperty("content")
        private String content;

        /** 站点名称；缺失时为 null。 */
        @JsonProperty("site_name")
        private String siteName;

        /** 站点图标 URL；缺失时为 null。 */
        @JsonProperty("site_icon")
        private String siteIcon;

        /**
         * 发布时间；未知时为 null。
         * <p>接口返回的是 UTC 的 RFC 3339 字符串（如 {@code 2026-06-01T00:00:00+00:00}），
         * 反序列化时由 Jackson 自动解析为 {@link Date}（带时区偏移，含 {@code Z} 与 {@code +00:00} 两种写法）。
         */
        @JsonProperty("date")
        private Date date;

        /** 网页权威性，0～4（-1 表示未知）；大于等于 3 为极权威结果。 */
        @JsonProperty("authority")
        private Integer authority;
    }
}
