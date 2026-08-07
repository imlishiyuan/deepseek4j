package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 工具函数定义（请求侧，嵌于 {@link Tool} / {@link ToolChoice} 的 function 字段）。
 */
@Data
public class ToolFunction {
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    /**
     * JSON Schema 对象，描述函数参数。省略则定义空参数列表。
     */
    @JsonProperty("parameters")
    private FunctionParameter parameters;
    /**
     * (Beta) 设为 true 确保输出符合 JSON Schema，默认 false。
     */
    @JsonProperty("strict")
    private Boolean strict;
}
