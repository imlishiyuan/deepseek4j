package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 回答正文中的引用（响应消息 {@code message.annotations}，DeepSeek 扩展字段）。
 *
 * <p>使用联网工具、回答引用了来源时返回。{@code content} 本身没有引用角标，
 * 引用信息在 {@code annotations} 中，{@code start_index} / {@code end_index}
 * 按 {@code content} 的 <b>Unicode 码点</b> 计数。没有引用时不返回该字段。
 */
@Data
public class Annotation {

    /** 引用类型，目前恒为 {@code url_citation}。 */
    @JsonProperty("type")
    private String type;

    /** 引用详情。 */
    @JsonProperty("url_citation")
    private UrlCitation urlCitation;

    /** 一条 URL 引用。 */
    @Data
    public static class UrlCitation {
        @JsonProperty("url")
        private String url;

        /** 来源标题；未知时为 URL 本身。 */
        @JsonProperty("title")
        private String title;

        /** 引用在 {@code content} 中的起始位置（Unicode 码点）。 */
        @JsonProperty("start_index")
        private Integer startIndex;

        /** 引用在 {@code content} 中的结束位置（Unicode 码点）。 */
        @JsonProperty("end_index")
        private Integer endIndex;
    }

    /** 便捷判断是否为 URL 引用。 */
    public boolean isUrlCitation() {
        return "url_citation".equals(type);
    }

    /** 便捷获取引用列表中的第一个 URL 引用（可能为 null）。 */
    public static UrlCitation firstUrlCitation(List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }
        return annotations.stream()
                .filter(annotation -> annotation != null && annotation.getUrlCitation() != null)
                .map(Annotation::getUrlCitation)
                .findFirst()
                .orElse(null);
    }
}
