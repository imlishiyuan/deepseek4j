package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 模型内容安全审核等级（{@code safety_check.level}，DeepSeek 扩展字段）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/safety">模型内容安全</a>
 */
@AllArgsConstructor
public enum SafetyCheckEnums {
    /** 默认检查，与不传 safety_check 相同 */
    LOW("low", "默认检查"),
    /** 开启基础风控，审核模型输入与输出 */
    HIGH("high", "基础风控"),
    ;
    public final String code;
    public final String desc;

    public static SafetyCheckEnums fromCode(String code) {
        for (SafetyCheckEnums anEnum : SafetyCheckEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

}
