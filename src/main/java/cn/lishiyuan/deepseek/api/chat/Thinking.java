package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.ThinkingEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 思考模式开关（请求体 {@code thinking}）。
 *
 * <p>只控制思考模式与非思考模式的转换。<b>推理强度已拆分为请求体的顶层字段
 * {@code reasoning_effort}</b>（见 {@link ChatRequest#getReasoningEffort()}），
 * 不再放在本对象里。
 *
 * <p>省略 {@code thinking} 或传 {@code null} 时，模式由 {@code reasoning_effort} 决定；
 * 两者都不提供时默认开启思考模式。提供本对象时 {@code type} 必填，且优先于 {@code reasoning_effort}。
 */
@Data
public class Thinking {

    /** 思考模式：enabled / disabled。提供 {@code thinking} 对象时该字段必填。 */
    @JsonProperty("type")
    private String type = ThinkingEnums.DISABLED.code;
}
