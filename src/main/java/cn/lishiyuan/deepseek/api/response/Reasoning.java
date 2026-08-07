package cn.lishiyuan.deepseek.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Reasoning {
    /** none/minimal/low/medium/high/xhigh/max，见 {@link cn.lishiyuan.deepseek.config.enums.ResponseReasoningEffortEnums} */
    @JsonProperty("effort")
    private String effort;
}
