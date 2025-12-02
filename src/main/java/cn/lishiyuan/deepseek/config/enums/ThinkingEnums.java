package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ThinkingEnums {
    DISABLED("disabled","disabled"),
    ENABLED("enabled","enabled"),

    ;
    public final String code;
    public final String desc;

    public static ThinkingEnums fromCode(String code){
        for (ThinkingEnums roleEnums : ThinkingEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
