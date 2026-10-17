package cn.lishiyuan.deepseek.api.search;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 网页抓取响应。
 * <p>抓取结果受目标站点的可用性与访问策略影响；抓不到可用正文时接口返回 404。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/fetch">网页抓取 API</a>
 */
@Data
public class FetchResponse extends BaseResponse {

    /** 页面标题；缺失时为空字符串。 */
    @JsonProperty("title")
    private String title;

    /** 抓取后的 Markdown 正文。 */
    @JsonProperty("content")
    private String content;
}
