package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ModelEnums {
    DEEPSEEK_V4_FLASH("deepseek-v4-flash","deepseek-v4-flash"),
    DEEPSEEK_V4_PRO("deepseek-v4-pro","deepseek-v4-pro"),
    /**
     * @deprecated 旧版模型，建议使用 {@link #DEEPSEEK_V4_FLASH}
     */
    @Deprecated
    DEEPSEEK_CHAT("deepseek-chat","deepseek-chat"),
    /**
     * @deprecated 旧版模型，建议使用 {@link #DEEPSEEK_V4_PRO}
     */
    @Deprecated
    DEEPSEEK_REASONER("deepseek-reasoner","deepseek-reasoner"),

    ;
    public final String code;
    public final String desc;

    public static ModelEnums fromCode(String code){
        for (ModelEnums roleEnums : ModelEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
