package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.RoleEnums;

/**
 * user 角色消息：用户提问。
 */
public class UserMessage extends NamedMessage {
    public UserMessage() {
        super(RoleEnums.USER.code);
    }
}
