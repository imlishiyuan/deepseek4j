package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 流式响应中的服务端工具事件（{@code choice.server_tool_event}，DeepSeek 扩展字段）。
 *
 * <p>与 {@code delta} 并列，一个 chunk 最多带一个事件：
 * <ul>
 *   <li>{@code step_reasoning_done}：第 {@code step} 步的思考已输出完；</li>
 *   <li>{@code call_started}：这一步的一次调用开始，带完整的 {@code action}；</li>
 *   <li>{@code call_completed}：调用结束，带 {@code status} 以及 {@code sources} / {@code page} / {@code error}（不带 signature）。</li>
 * </ul>
 *
 * <p>完整的 {@code server_tool_calls} 与 {@code annotations} 只在带 {@code finish_reason}
 * 的最后一个 chunk 的 {@code delta} 中发送一次。
 */
@Data
public class ServerToolEvent {

    /** step_reasoning_done / call_started / call_completed */
    @JsonProperty("type")
    private String type;

    /** 事件所属的步序号。 */
    @JsonProperty("step")
    private Integer step;

    /** 事件对应的调用（call_started / call_completed 时返回）。 */
    @JsonProperty("call")
    private ServerToolCall call;
}
