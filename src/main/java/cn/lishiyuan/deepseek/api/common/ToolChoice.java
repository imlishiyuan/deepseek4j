package cn.lishiyuan.deepseek.api.common;

import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** tool_choice 对象形式便捷构造：{"type":"function","name":"..."} 或 {"type":"web_search"} */
@Data
public class ToolChoice {
    @JsonProperty("type")
    private String type = ToolTypeEnums.FUNCTION.code;

    @JsonProperty("name")
    private String name;
}
