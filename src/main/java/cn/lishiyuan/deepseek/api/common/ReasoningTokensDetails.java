package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * token 明细：推理 token（reasoning_tokens）与安全审核 token（safety_tokens）。
 * chat/FIM 的 completion_tokens_details 与 responses 的 output_tokens_details 结构一致，共用此类型。
 */
@Data
public class ReasoningTokensDetails {
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;

    /**
     * 安全审核产生的 token 数（DeepSeek 扩展字段），已包含在 completion_tokens 中。
     * 仅在请求 {@code safety_check.level=high} 时返回。
     */
    @JsonProperty("safety_tokens")
    private Integer safetyTokens;
}
