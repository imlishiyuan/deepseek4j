package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 服务端工具打开/阅读的网页信息（{@link ServerToolCall#getPage()}），仅供展示。
 *
 * <p>{@code published_at} 格式不固定：阅读链接为网页上提取到的日期，
 * 深度思考搜索中打开的网页为 RFC 3339 时间戳，两者都可能为 {@code null}。
 * 该对象中的字段即使被修改也不影响回传效果。
 */
@Data
public class ServerToolPage {

    @JsonProperty("url")
    private String url;

    @JsonProperty("title")
    private String title;

    @JsonProperty("published_at")
    private String publishedAt;
}
