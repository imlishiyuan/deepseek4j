package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * Responses API 文本输出格式（text.format.type）
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@AllArgsConstructor
public enum ResponseTextFormatEnums {
    TEXT("text","普通文本（默认）"),
    JSON_OBJECT("json_object","JSON 对象"),
    JSON_SCHEMA("json_schema","按 JSON Schema 输出"),
    ;
    public final String code;
    public final String desc;

    public static ResponseTextFormatEnums fromCode(String code){
        for (ResponseTextFormatEnums anEnum : ResponseTextFormatEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }
}
