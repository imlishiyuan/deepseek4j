package cn.lishiyuan.deepseek.config.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * tool_choice 的字符串取值形式。
 * 也可使用对象形式 {"type":"function","function":{"name":"..."}} 强制调用指定 tool。
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@AllArgsConstructor
public enum ToolChoiceEnums {
    NONE("none","不调用任何 tool"),
    AUTO("auto","自动决定（有 tool 时默认）"),
    REQUIRED("required","必须调用一个或多个 tool"),
    ;
    @JsonValue
    public final String code;
    public final String desc;

    public static ToolChoiceEnums fromCode(String code){
        for (ToolChoiceEnums anEnum : ToolChoiceEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }

}
