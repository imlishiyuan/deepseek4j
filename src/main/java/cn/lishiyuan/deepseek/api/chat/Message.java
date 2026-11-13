package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Message {
    @JsonProperty("content")
    private String content;

    @JsonProperty("reasoning_content")
    private String reasoningContent;

    @JsonProperty("role")
    private String role;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    /**
     * 回答正文中的引用（DeepSeek 扩展字段，使用联网工具时返回）。
     * <p>{@code content} 中没有引用角标，引用信息在这里，位置按 content 的 Unicode 码点计数。
     */
    @JsonProperty("annotations")
    private List<Annotation> annotations;

    /**
     * 服务端工具调用（DeepSeek 扩展字段，使用联网工具时返回）。
     * <p>每个元素是一步：这一步的思考，以及这一步发起的搜索/抓取调用。多轮对话时需原样回传。
     */
    @JsonProperty("server_tool_calls")
    private List<ServerToolStep> serverToolCalls;

    /** 会话标题（DeepSeek 扩展字段），请求中 {@code title.enabled=true} 时返回。 */
    @JsonProperty("title")
    private String title;

    /** 请求中的图片未通过内容安全审核时，指出被拦截的图片（DeepSeek 扩展字段）。 */
    @JsonProperty("refusal")
    private String refusal;
}
