package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * Responses API 响应状态（status）
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@AllArgsConstructor
public enum ResponseStatusEnums {
    IN_PROGRESS("in_progress","生成中"),
    COMPLETED("completed","完成"),
    INCOMPLETE("incomplete","未完成"),
    FAILED("failed","失败"),
    ;
    public final String code;
    public final String desc;

    public static ResponseStatusEnums fromCode(String code){
        for (ResponseStatusEnums anEnum : ResponseStatusEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }
}
