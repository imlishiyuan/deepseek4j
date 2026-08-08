package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ToolCall {
    /** 流式 delta 中标识第几个工具调用，用于多工具并发时按 index 合并；非流式响应中不存在 */
    @JsonProperty("index")
    private Integer index;

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("function")
    private ToolCallFunction function;
}
