package cn.lishiyuan.deepseek.api.chat.msg;

import cn.lishiyuan.deepseek.config.enums.RoleEnums;

/**
 * system 角色消息：设定助手行为。
 */
public class SystemMessage extends NamedMessage {
    public SystemMessage() {
        super(RoleEnums.SYSTEM.code);
    }
}
