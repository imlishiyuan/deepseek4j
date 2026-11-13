package cn.lishiyuan.deepseek.api.message;

import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.common.ServerToolUse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Anthropic Messages API 响应。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/create-message">Messages API</a>
 */
@Data
public class MessageResponse extends BaseResponse {

    @JsonProperty("id")
    private String id;

    /** 恒为 message */
    @JsonProperty("type")
    private String type;

    @JsonProperty("role")
    private String role;

    @JsonProperty("model")
    private String model;

    /** 内容块数组：text / thinking / server_tool_use / 各类 tool_result / refusal */
    @JsonProperty("content")
    private List<MessageContentBlock> content;

    /** end_turn / max_tokens / refusal / tool_use */
    @JsonProperty("stop_reason")
    private String stopReason;

    @JsonProperty("stop_sequence")
    private String stopSequence;

    @JsonProperty("usage")
    private Usage usage;

    /** 会话标题（DeepSeek 扩展字段），请求开启 title 且生成时出现。 */
    @JsonProperty("title")
    private String title;

    /** 用量。 */
    @Data
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        private Integer outputTokens;

        /** 恒为 0 */
        @JsonProperty("cache_creation_input_tokens")
        private Integer cacheCreationInputTokens;

        /** 命中上下文缓存的输入 token */
        @JsonProperty("cache_read_input_tokens")
        private Integer cacheReadInputTokens;

        @JsonProperty("service_tier")
        private String serviceTier;

        /** 计费的服务端工具使用次数（DeepSeek 扩展字段，使用联网工具时返回）。 */
        @JsonProperty("server_tool_use")
        private ServerToolUse serverToolUse;

        /** 输出 token 明细：thinking_tokens / safety_tokens（DeepSeek 扩展字段）。 */
        @JsonProperty("output_tokens_details")
        private OutputTokensDetails outputTokensDetails;

        /** 输出 token 明细（DeepSeek 扩展字段）。 */
        @Data
        public static class OutputTokensDetails {
            /** 思考 token 数 */
            @JsonProperty("thinking_tokens")
            private Integer thinkingTokens;

            /** 安全审核 token 数 */
            @JsonProperty("safety_tokens")
            private Integer safetyTokens;
        }
    }
}
