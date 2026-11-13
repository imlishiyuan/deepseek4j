package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 思考模式推理强度（请求体顶层字段 {@code reasoning_effort}）。
 *
 * <p>{@code none} 关闭思考模式；{@code low} / {@code high} / {@code max} 开启思考模式，默认 {@code high}。
 * 出于兼容考虑，服务端会把 {@code minimal} 映射为 {@code low}、{@code medium} / {@code xhigh} 映射为 {@code high}。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/">Chat Completions API</a>
 */
@AllArgsConstructor
public enum ThinkingEffortEnums {
    NONE("none","关闭思考模式"),
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
