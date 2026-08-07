package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum FuncParamEnums {
    OBJ("object","对象"),
    STR("string","字符串"),
    NUM("number","数字"),
    INT("integer","整数"),
    BOOL("boolean","布尔"),
    ARR("array","数组"),
    ENUM("enum","枚举"),
    ANY_OF("anyOf","anyOf"),
    ;
    public final String code;
    public final String desc;

    public static FuncParamEnums fromCode(String code){
        for (FuncParamEnums anEnum : FuncParamEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }

}
