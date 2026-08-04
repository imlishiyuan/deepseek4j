package cn.lishiyuan.deepseek.api.fim;

import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StreamFimResponse extends BaseStreamResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("created")
    private Integer created;
    @JsonProperty("model")
    private String model;
    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
    // text_completion
    @JsonProperty("object")
    private String object;

    @JsonProperty("usage")
    private Usage usage;

    @Data
    public static class Choice {
        @JsonProperty("finish_reason")
        private String finishReason;

        @JsonProperty("index")
        private Integer index;

        @JsonProperty("text")
        private String text;

        @JsonProperty("logprobs")
        private Logprobs logprobs;
    }

    @Data
    public static class Logprobs {
        @JsonProperty("text_offset")
        private List<Integer> textOffset;

        @JsonProperty("token_logprobs")
        private List<Double> tokenLogprobs;

        private List<String> tokens;

        @JsonProperty("top_logprobs")
        private List<Object> topLogprobs;
    }

    @Data
    public static class Usage {
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
        private CompletionTokensDetails completionTokensDetails;
    }

    @Data
    public static class CompletionTokensDetails {
        @JsonProperty("reasoning_tokens")
        private Integer reasoningTokens;
    }
}
