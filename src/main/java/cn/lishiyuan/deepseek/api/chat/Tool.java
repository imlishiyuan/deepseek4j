package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 工具引用 {type, function}：用于请求的 tools 数组（工具定义，function 带 name/description/parameters），
 * 也用于 tool_choice 对象形式（指定工具，function 仅带 name）。结构一致，共用此类型。
 */
@Data
public class Tool {
    // function
    @JsonProperty("type")
    private String type = ToolTypeEnums.FUNCTION.code;

    @JsonProperty("function")
    private ToolFunction function;
}
