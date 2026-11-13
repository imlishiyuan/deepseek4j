package cn.lishiyuan.deepseek.api.common;

import cn.lishiyuan.deepseek.config.enums.SafetyCheckEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 模型内容安全开关（请求体 {@code safety_check}，DeepSeek 扩展字段）。
 *
 * <p>{@code level} 为 {@code high} 时开启基础风控，审核模型输入与输出；命中风险时请求返回
 * {@code 400 Content Exists Risk}，或响应以拒答结束（{@code finish_reason=content_filter}、
 * {@code message.refusal}）。{@code low} 为默认检查，等价于不传。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/safety">模型内容安全</a>
 */
@Data
public class SafetyCheck {

    /** 审核等级：low（默认）/ high。 */
    @JsonProperty("level")
    private String level = SafetyCheckEnums.LOW.code;

    public static SafetyCheck of(String level) {
        SafetyCheck safetyCheck = new SafetyCheck();
        safetyCheck.setLevel(level);
        return safetyCheck;
    }

    /** 开启基础风控。 */
    public static SafetyCheck high() {
        return of(SafetyCheckEnums.HIGH.code);
    }

    /** 默认检查（等价于不传）。 */
    public static SafetyCheck low() {
        return of(SafetyCheckEnums.LOW.code);
    }
}
