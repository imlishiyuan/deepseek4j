package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * token 明细中的推理 token 数（reasoning_tokens）。
 * chat/FIM 的 completion_tokens_details 与 responses 的 output_tokens_details 结构一致，共用此类型。
 */
@Data
public class ReasoningTokensDetails {
    @JsonProperty("reasoning_tokens")
    private Integer reasoningTokens;
}
