package cn.lishiyuan.deepseek.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoleEnums {
    SYSTEM("system","系统"),
    USER("user","用户"),
    ASSISTANT("assistant","助手")

    ;
    public final String code;
    public final String desc;

    public static RoleEnums fromCode(String code){
        for (RoleEnums roleEnums : RoleEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
