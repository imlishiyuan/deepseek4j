package cn.lishiyuan.deepseek.api.response;

import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.common.ReasoningTokensDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Responses API 非流式响应。
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@Data
public class ResponseResult extends BaseResponse {
    @JsonProperty("id")
    private String id;

    /** 固定 "response" */
    @JsonProperty("object")
    private String object;

    @JsonProperty("created_at")
    private Integer createdAt;

    /** in_progress / completed / incomplete / failed */
    @JsonProperty("status")
    private String status;

    @JsonProperty("model")
    private String model;

    @JsonProperty("error")
    private Error error;

    @JsonProperty("incomplete_details")
    private IncompleteDetails incompleteDetails;

    @JsonProperty("output")
    private List<OutputItem> output;

    @JsonProperty("usage")
    private Usage usage;

    /**
     * 输出项：type 为 message / reasoning / function_call / web_search_call。
     * 不同 type 使用不同字段子集。
     */
    @Data
    public static class OutputItem {
        /** message / reasoning / function_call / web_search_call */
        @JsonProperty("type")
        private String type;

        @JsonProperty("id")
        private String id;

        /** in_progress / completed / incomplete */
        @JsonProperty("status")
        private String status;

        /** message 项固定 "assistant" */
        @JsonProperty("role")
        private String role;

        /** message: output_text 块；reasoning: reasoning_text 块 */
        @JsonProperty("content")
        private List<ContentBlock> content;

        /** function_call: 用于回传结果的标识 */
        @JsonProperty("call_id")
        private String callId;

        /** function_call: 函数名 */
        @JsonProperty("name")
        private String name;

        /** function_call: 模型生成的 JSON 参数字符串（不保证合法） */
        @JsonProperty("arguments")
        private String arguments;

        /** web_search_call: 搜索动作描述（search/open_page/find_in_page） */
        @JsonProperty("action")
        private Object action;
    }

    @Data
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("input_tokens_details")
        private InputTokensDetails inputTokensDetails;

        @JsonProperty("output_tokens")
        private Integer outputTokens;

        @JsonProperty("output_tokens_details")
        private ReasoningTokensDetails outputTokensDetails;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    @Data
    public static class InputTokensDetails {
        /** 命中上下文缓存的输入 token 数 */
        @JsonProperty("cached_tokens")
        private Integer cachedTokens;
    }

    @Data
    public static class Error {
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;
    }

    @Data
    public static class IncompleteDetails {
        /** max_output_tokens / content_filter */
        @JsonProperty("reason")
        private String reason;
    }
}
