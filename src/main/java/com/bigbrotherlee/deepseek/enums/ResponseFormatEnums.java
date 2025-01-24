package com.bigbrotherlee.deepseek.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResponseFormatEnums {
    TEXT("text","text"),
    JSON_OBJECT("json_object","json"),
    ;
    public final String code;
    public final String desc;

    public static ResponseFormatEnums fromCode(String code){
        for (ResponseFormatEnums roleEnums : ResponseFormatEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
