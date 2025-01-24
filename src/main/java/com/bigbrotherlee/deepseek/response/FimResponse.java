package com.bigbrotherlee.deepseek.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class FimResponse {
    private String id;
    private Integer created;
    private String model;
    @JSONField(name = "choices")
    private List<Choice> choices;

    @JSONField(name = "system_fingerprint")
    private String systemFingerprint;
    // text_completion
    private String object;

    @JSONField(name = "usage")
    private Usage usage;

    @Data
    public static class Choice {
        @JSONField(name = "finish_reason")
        private String finishReason;

        @JSONField(name = "index")
        private Integer index;

        @JSONField(name = "text")
        private String text;

        @JSONField(name = "logprobs")
        private Logprobs logprobs;
    }

    @Data
    public static class Logprobs {
        @JSONField(name = "text_offset")
        private List<Integer> textOffset;

        @JSONField(name = "token_logprobs")
        private List<Double> tokenLogprobs;

        private List<String> tokens;

        @JSONField(name = "top_logprobs")
        private List<Object> topLogprobs;
    }

    @Data
    public static class Usage {
        @JSONField(name = "completion_tokens")
        private Integer completionTokens;

        @JSONField(name = "prompt_tokens")
        private Integer promptTokens;

        @JSONField(name = "prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;

        @JSONField(name = "prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;

        @JSONField(name = "total_tokens")
        private Integer totalTokens;

        @JSONField(name = "completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;
    }

    @Data
    public static class CompletionTokensDetails {
        @JSONField(name = "reasoning_tokens")
        private Integer reasoningTokens;
    }
}
