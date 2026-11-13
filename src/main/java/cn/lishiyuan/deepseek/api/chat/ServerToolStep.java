package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 服务端工具调用的一步（响应消息 {@code message.server_tool_calls} 的元素）。
 *
 * <p>每个元素是一步：这一步的思考（{@code reasoningContent}），以及这一步发起的调用（{@code calls}）。
 * 注意 {@code Message.reasoningContent} 只包含最后一步的思考，之前各步的思考在各自的条目里。
 */
@Data
public class ServerToolStep {

    /** 步的序号，从 0 开始。 */
    @JsonProperty("index")
    private Integer index;

    /**
     * 这一步的思考。开启思考时总是字符串（没有思考时为空字符串）；未开启思考时不返回该字段。
     */
    @JsonProperty("reasoning_content")
    private String reasoningContent;

    /** 这一步发起的调用，按发起顺序排列。 */
    @JsonProperty("calls")
    private List<ServerToolCall> calls;
}
