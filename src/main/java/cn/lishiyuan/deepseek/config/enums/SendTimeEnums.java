package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 发送时机
 */
@AllArgsConstructor
public enum SendTimeEnums {
    NEXT_REQUEST("next_request","跟随下个请求"),
    NEXT_TURN("next_turn","下个轮次"),
    ;
    public final String code;
    public final String desc;

    public static SendTimeEnums fromCode(String code){
        for (SendTimeEnums roleEnums : SendTimeEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
