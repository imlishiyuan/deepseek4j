package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RoleEnums {
    SYSTEM("system","系统"),
    USER("user","用户"),
    ASSISTANT("assistant","助手"),
    TOOL("tool","工具"),
    /** 用户元信息标记（DeepSeek 扩展角色），content 形如 "2026-11-11,星期三,新加坡,Web,中文" */
    LATEST_REMINDER("latest_reminder","用户元信息标记")
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
