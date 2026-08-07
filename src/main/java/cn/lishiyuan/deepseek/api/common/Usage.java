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
}
