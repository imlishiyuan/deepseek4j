package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 计费的服务端工具使用次数（{@code usage.server_tool_use}，DeepSeek 扩展字段）。
 *
 * <p>使用联网工具时返回；各项计数均为 0 时省略。这里是<b>计费次数</b>，
 * 与响应中实际的搜索结果块 / 抓取结果块数量不一定相同。
 */
@Data
public class ServerToolUse {

    /** 响应中搜索结果块的数量（仅供展示，不是计费次数；Messages / Responses 才有） */
    @JsonProperty("web_search_requests")
    private Integer webSearchRequests;

    /** 响应中抓取结果块的数量（仅供展示，不是计费次数；Messages / Responses 才有） */
    @JsonProperty("web_fetch_requests")
    private Integer webFetchRequests;

    /** 计费的搜索次数，不含图片搜索。 */
    @JsonProperty("tool_search")
    private Integer toolSearch;

    /** 计费的网页抓取次数。 */
    @JsonProperty("tool_fetch")
    private Integer toolFetch;

    /** 计费的图片搜索次数。 */
    @JsonProperty("tool_image_search")
    private Integer toolImageSearch;
}
