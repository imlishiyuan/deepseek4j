package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.chat.msg.ChatRequestMessage;
import cn.lishiyuan.deepseek.api.common.SafetyCheck;
import cn.lishiyuan.deepseek.api.common.StreamOptions;
import cn.lishiyuan.deepseek.api.common.TitleOptions;
import cn.lishiyuan.deepseek.api.common.ToolChoice;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@Data
public class ChatRequest extends BaseRequest<ChatResponse> {

    @JsonProperty("messages")
    private List<ChatRequestMessage> messages;

    // deepseek-v4-flash, deepseek-v4-pro
    @JsonProperty("model")
    private String model = ModelEnums.DEEPSEEK_FLASH.code;
    /**
     * 思考模式
     */
    private Thinking thinking = new Thinking();

    /**
     * 思考强度（请求体<b>顶层</b>字段，DeepSeek 扩展）。
     * none 关闭思考；low / high / max 开启思考，默认 high。
     * 省略 {@code thinking} 时模式由它决定；提供 {@code thinking} 时其 type 优先。
     */
    @JsonProperty("reasoning_effort")
    private String reasoningEffort;

    /**
     * @deprecated 已废弃，传入无效果。见 API 文档。
     */
    @Deprecated
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * @deprecated 已废弃，传入无效果。见 API 文档。
     */
    @Deprecated
    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("stream")
    private final boolean stream = false;

    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("tools")
    private List<Tool> tools;

    /**
     * 可取字符串 "none"/"auto"/"required"（见 {@link cn.lishiyuan.deepseek.config.enums.ToolChoiceEnums}），
     * 或对象形式 {"type":"function","function":{"name":"..."}} 强制调用指定 tool。 {@link ToolChoice}
     */
    @JsonProperty("tool_choice")
    private Object toolChoice;

    @JsonProperty("logprobs")
    private Boolean logprobs;

    @JsonProperty("top_logprobs")
    private Integer topLogprobs;

    /**
     * 自定义 user_id，字符集 [a-zA-Z0-9\-_]，最大 512 字符。
     * 用于内容安全处理、KVCache 缓存隔离、调度隔离。
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 模型内容安全开关（DeepSeek 扩展字段）。
     * level=high 时开启基础风控，命中风险时返回 400 或以拒答结束。
     */
    @JsonProperty("safety_check")
    private SafetyCheck safetyCheck;

    /**
     * 会话标题开关（DeepSeek 扩展字段）。
     * 传 {"enabled": true} 后，响应在 message.title（流式为最后一个 chunk 的 delta.title）返回简短标题。
     */
    @JsonProperty("title")
    private TitleOptions title;

    @Override
    public Class<ChatResponse> getResponseClass() {
        return ChatResponse.class;
    }

    @Override
    public String getPath() {
        return "chat/completions";
    }

    public static ChatRequest create(List<ChatRequestMessage> messages, String model) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(messages);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
