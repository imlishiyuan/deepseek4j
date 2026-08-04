package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * Responses API 推理强度（reasoning.effort）
 * <p>none 关闭思考；minimal/low -> low；medium/high/xhigh -> high；max -> max。
 * 不传时由服务端按默认（思考开启）处理。
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@AllArgsConstructor
public enum ResponseReasoningEffortEnums {
    NONE("none","关闭思考"),
    MINIMAL("minimal","最低（映射 low）"),
    LOW("low","低"),
    MEDIUM("medium","中（映射 high）"),
    HIGH("high","高"),
    XHIGH("xhigh","较高（映射 high）"),
    MAX("max","最大"),
    ;
    public final String code;
    public final String desc;

    public static ResponseReasoningEffortEnums fromCode(String code){
        for (ResponseReasoningEffortEnums anEnum : ResponseReasoningEffortEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }
}
