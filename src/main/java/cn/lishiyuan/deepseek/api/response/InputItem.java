package cn.lishiyuan.deepseek.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 输入项：type 为 message / function_call / function_call_output / reasoning / web_search_call。
 * message 项有 type 时可省略 role 之外的鉴别。不同 type 使用不同字段子集。
 */
@Data
public class InputItem {
    /** message / function_call / function_call_output / reasoning / web_search_call */
    @JsonProperty("type")
    private String type;

    /** message: user / assistant / system / developer（developer 视为 system） */
    @JsonProperty("role")
    private String role;

    /** message: 字符串或 List<ContentBlock>；reasoning: List<ContentBlock>；多模态时含 input_text/input_image 块 */
    @JsonProperty("content")
    private Object content;

    /** function_call / function_call_output: 关联调用与结果的标识 */
    @JsonProperty("call_id")
    private String callId;

    /** function_call: 函数名 */
    @JsonProperty("name")
    private String name;

    /** function_call: JSON 参数字符串 */
    @JsonProperty("arguments")
    private String arguments;

    /** function_call_output: 函数调用结果，可为字符串或 List<ContentBlock>（多模态可含 input_image 块）；custom_tool_call_output 输出可用 */
    @JsonProperty("output")
    private Object output;
}
