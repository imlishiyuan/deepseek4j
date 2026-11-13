package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Usage {
    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("prompt_cache_hit_tokens")
    private Integer promptCacheHitTokens;

    @JsonProperty("prompt_cache_miss_tokens")
    private Integer promptCacheMissTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

    @JsonProperty("completion_tokens_details")
    private ReasoningTokensDetails completionTokensDetails;

    /**
     * 计费的服务端工具使用次数（DeepSeek 扩展字段，使用联网工具时返回；各项为 0 时省略）。
     */
    @JsonProperty("server_tool_use")
    private ServerToolUse serverToolUse;
}
