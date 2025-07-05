package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class StreamChatResponse extends BaseStreamResponse {
    @JSONField(name = "id")
    private String id;
    @JSONField(name = "created")
    private Integer created;
    @JSONField(name = "model")
    private String model;
    @JSONField(name = "choices")
    private List<Choice> choices;

    @JSONField(name = "system_fingerprint")
    private String systemFingerprint;
    // chat.completion.chunk
    @JSONField(name = "object")
    private String object;

    @JSONField(name = "usage")
    private Usage usage;

    @Data
    public static class Choice {
        @JSONField(name = "finish_reason")
        private String finishReason;

        @JSONField(name = "index")
        private Integer index;

        @JSONField(name = "delta")
        private Message message;

        @JSONField(name = "logprobs")
        private Logprobs logprobs;
    }

    @Data
    public static class Message {
        @JSONField(name = "content")
        private String content;

        @JSONField(name = "reasoning_content")
        private String reasoningContent;

        @JSONField(name = "role")
        private String role;

        @JSONField(name = "tool_calls")
        private List<ToolCall> toolCalls;
    }

    @Data
    public static class ToolCall {
        @JSONField(name = "id")
        private String id;

        @JSONField(name = "type")
        private String type;

        @JSONField(name = "function")
        private Function function;
    }

    @Data
    public static class Function {
        @JSONField(name = "name")
        private String name;

        @JSONField(name = "arguments")
        private String arguments;
    }

    @Data
    public static class Logprobs {
        @JSONField(name = "content")
        private List<TokenLogprob> content;

        @JSONField(name = "top_logprobs")
        private List<TokenLogprob> topLogprobs;
    }

    @Data
    public static class TokenLogprob {
        @JSONField(name = "token")
        private String token;

        @JSONField(name = "logprob")
        private Double logprob;

        @JSONField(name = "bytes")
        private List<Integer> bytes;
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
