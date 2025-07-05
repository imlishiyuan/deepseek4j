package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ModelEnums {
    DEEPSEEK_CHAT("deepseek-chat","deepseek-chat"),
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
