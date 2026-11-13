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

    /**
     * 输入项内容，可用类型（按 {@link #getType()} 区分）：
     * <ul>
     *   <li>{@code message}：{@link String} 或 {@code List<ContentBlock>}（多模态时含 input_text / input_image 块）；</li>
     *   <li>{@code reasoning}：{@code List<ContentBlock>}（reasoning_text 块）。</li>
     * </ul>
     */
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

    /**
     * 工具输出，可用类型：
     * <ul>
     *   <li>{@link String}：纯文本结果；</li>
     *   <li>{@code List<ContentBlock>}：内容块数组（多模态可含 input_image 块）。</li>
     * </ul>
     */
    @JsonProperty("output")
    private Object output;
}
