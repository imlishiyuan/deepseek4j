package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ModelEnums {
    DEEPSEEK_FLASH("deepseek-flash","deepseek-v4.1-flash"),

    DEEPSEEK_V4_PRO("deepseek-v4-pro","deepseek-v4-pro"),

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
