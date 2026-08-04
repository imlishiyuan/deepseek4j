package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 思考模式推理强度（thinking.reasoning_effort）
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@AllArgsConstructor
public enum ThinkingEffortEnums {
    LOW("low","低"),
    HIGH("high","高"),
    MAX("max","最大"),
    ;
    public final String code;
    public final String desc;

    public static ThinkingEffortEnums fromCode(String code){
        for (ThinkingEffortEnums anEnum : ThinkingEffortEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }

}
