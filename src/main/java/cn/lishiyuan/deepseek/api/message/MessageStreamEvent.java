package cn.lishiyuan.deepseek.api.message;

import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Anthropic Messages API 流式事件。
 *
 * <p>{@code type} 取自每个事件的 {@code data.type}（服务端在 data 中也会带上 type），包括：
 * message_start / content_block_start / content_block_delta / content_block_stop /
 * message_delta / message_stop / ping / error。
 *
 * <p>不同事件使用不同字段：
 * <ul>
 *   <li>message_start：{@code message}（含 usage 的初始值）；</li>
 *   <li>content_block_start：{@code index} + {@code content_block}；</li>
 *   <li>content_block_delta：{@code index} + {@code delta}（text/thinking/signature/citation）；</li>
 *   <li>message_delta：{@code delta}（stop_reason/stop_sequence/title）+ {@code usage}（最终用量）；</li>
 *   <li>message_stop：结束事件。</li>
 * </ul>
 */
@Data
public class MessageStreamEvent extends BaseStreamResponse {

    /** 事件类型 */
    @JsonProperty("type")
    private String type;

    /** 内容块索引（content_block_* 事件） */
    @JsonProperty("index")
    private Integer index;

    /** message_start 事件携带的消息对象（content 为空数组，usage 为第一步用量） */
    @JsonProperty("message")
    private MessageResponse message;

    /** content_block_start 事件携带的内容块 */
    @JsonProperty("content_block")
    private MessageContentBlock contentBlock;

    /** content_block_delta / message_delta 事件的增量 */
    @JsonProperty("delta")
    private Delta delta;

    /** message_delta 事件携带的最终用量 */
    @JsonProperty("usage")
    private MessageResponse.Usage usage;

    /** 增量内容。 */
    @Data
    public static class Delta {
        /** text_delta / thinking_delta / signature_delta / citations_delta */
        @JsonProperty("type")
        private String type;

        @JsonProperty("text")
        private String text;

        @JsonProperty("thinking")
        private String thinking;

        @JsonProperty("signature")
        private String signature;

        /** citations_delta 的引用 */
        @JsonProperty("citation")
        private Object citation;

        /** message_delta：停止原因 */
        @JsonProperty("stop_reason")
        private String stopReason;

        @JsonProperty("stop_sequence")
        private String stopSequence;

        /** message_delta：会话标题（DeepSeek 扩展字段） */
        @JsonProperty("title")
        private String title;
    }
}
