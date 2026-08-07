package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.ThinkingEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Thinking {
    @JsonProperty("type")
    private String type = ThinkingEnums.DISABLED.code;
    /**
     * 推理强度，仅 type=enabled 时生效。可选 low/high/max。
     * 不设置时由服务端按默认值（high）处理。
     */
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;
}
