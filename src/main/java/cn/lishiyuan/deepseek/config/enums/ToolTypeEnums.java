package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ToolTypeEnums {
    FUNCTION("function","function"),

    ;
    public final String code;
    public final String desc;

    public static ToolTypeEnums fromCode(String code){
        for (ToolTypeEnums roleEnums : ToolTypeEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
