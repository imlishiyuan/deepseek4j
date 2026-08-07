package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 工具调用中的函数（响应侧，嵌于 {@link ToolCall} 的 function 字段）。
 */
@Data
public class ToolCallFunction {
    @JsonProperty("name")
    private String name;

    @JsonProperty("arguments")
    private String arguments;
}
