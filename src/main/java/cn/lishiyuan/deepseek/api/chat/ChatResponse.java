package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse extends BaseResponse {
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
    // chat.completion

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

        @JsonProperty("message")
        private Message message;

        @JsonProperty("logprobs")
        private Logprobs logprobs;
    }

    @Data
    public static class Message {
        @JsonProperty("content")
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;

        @JsonProperty("role")
        private String role;

        @JsonProperty("tool_calls")
        private List<ToolCall> toolCalls;
    }

    @Data
    public static class ToolCall {
        @JsonProperty("id")
        private String id;

        @JsonProperty("type")
        private String type;

        @JsonProperty("function")
        private Function function;
    }

    @Data
    public static class Function {
        @JsonProperty("name")
        private String name;

        @JsonProperty("arguments")
        private String arguments;
    }

    @Data
    public static class Logprobs {
        @JsonProperty("content")
        private List<TokenLogprob> content;
        @JsonProperty("reasoning_content")
        private List<TokenLogprob> reasoningContent;
    }

    @Data
    public static class TokenLogprob {
        @JsonProperty("token")
        private String token;

        @JsonProperty("logprob")
        private Double logprob;

        @JsonProperty("bytes")
        private List<Integer> bytes;

        @JsonProperty("top_logprobs")
        private List<TokenLogprob> topLogprobs;
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
