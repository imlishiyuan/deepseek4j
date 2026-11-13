package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 服务端搜索/抓取结果中的一条来源（{@link ServerToolCall#getSources()}）。
 *
 * <p>展示字段（{@code site_name} / {@code site_icon} / {@code snippet}）为 DeepSeek 扩展字段，
 * 可能为 {@code null}；回传给服务端时会被忽略。
 */
@Data
public class ServerToolSource {

    /** 来源类型，通常为 {@code url}。 */
    @JsonProperty("type")
    private String type;

    /** 来源链接。 */
    @JsonProperty("url")
    private String url;

    /** 标题。 */
    @JsonProperty("title")
    private String title;

    /** 与查询相关的摘要。 */
    @JsonProperty("snippet")
    private String snippet;

    /** 站点名称，可能为 null。 */
    @JsonProperty("site_name")
    private String siteName;

    /** 站点图标 URL，可能为 null。 */
    @JsonProperty("site_icon")
    private String siteIcon;

    /** 发布时间，RFC 3339 格式的 UTC 时间戳或 null。 */
    @JsonProperty("published_at")
    private String publishedAt;

    /** 图片搜索时才有：图片在原网站上的地址，仅供展示，可能不存在或不可访问。 */
    @JsonProperty("image_url")
    private String imageUrl;
}
