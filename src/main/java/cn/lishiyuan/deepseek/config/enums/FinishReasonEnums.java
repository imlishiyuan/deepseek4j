package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 生成停止原因（choices[].finish_reason）
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@AllArgsConstructor
public enum FinishReasonEnums {
    STOP("stop","模型自然停止或遇到 stop 序列"),
    LENGTH("length","达到上下文长度或 max_tokens 限制"),
    CONTENT_FILTER("content_filter","输出触发过滤策略"),
    TOOL_CALLS("tool_calls","模型发起 tool 调用"),
    INSUFFICIENT_SYSTEM_RESOURCE("insufficient_system_resource","系统推理资源不足，生成被打断"),
    ;
    public final String code;
    public final String desc;

    public static FinishReasonEnums fromCode(String code){
        for (FinishReasonEnums anEnum : FinishReasonEnums.values()) {
            if (anEnum.code.equals(code)){
                return anEnum;
            }
        }
        return null;
    }

}
