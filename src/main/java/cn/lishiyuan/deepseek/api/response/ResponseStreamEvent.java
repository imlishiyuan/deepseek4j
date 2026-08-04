package cn.lishiyuan.deepseek.api.response;

import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Responses API 流式事件。
 * <p>type 由 SSE 的 event: 行注入（data 中不含顶层 type），包括：
 * response.created / response.output_item.added / response.reasoning_text.delta /
 * response.output_text.delta / response.completed / response.incomplete / response.failed。
 * <p>不同 type 使用不同字段子集：
 * <ul>
 *   <li>created/completed/incomplete/failed -> response</li>
 *   <li>output_item.added -> item + output_index</li>
 *   <li>output_text.delta / reasoning_text.delta -> delta + item_id + output_index + content_index</li>
 * </ul>
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@Data
public class ResponseStreamEvent extends BaseStreamResponse {
    /** 事件类型，由 SSE event: 行注入 */
    @JsonProperty("type")
    private String type;

    @JsonProperty("sequence_number")
    private Integer sequenceNumber;

    @JsonProperty("output_index")
    private Integer outputIndex;

    @JsonProperty("content_index")
    private Integer contentIndex;

    @JsonProperty("item_id")
    private String itemId;

    /** delta 事件中的增量文本 */
    @JsonProperty("delta")
    private String delta;

    /** created/completed/incomplete/failed 事件携带的完整响应对象 */
    @JsonProperty("response")
    private ResponseResult response;

    /** output_item.added 事件携带的输出项 */
    @JsonProperty("item")
    private ResponseResult.OutputItem item;
}
