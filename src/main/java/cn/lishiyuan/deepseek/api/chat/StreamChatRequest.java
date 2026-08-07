package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.common.StreamOptions;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@Data
public class StreamChatRequest extends BaseStreamRequest<StreamChatResponse> {

    @JsonProperty("messages")
    private List<ChatRequestMessage> messages;

    // deepseek-v4-flash, deepseek-v4-pro
    @JsonProperty("model")
    private String model;
    /**
     * 思考模式
     */
    private Thinking thinking = new Thinking();

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
    private final boolean stream = true;

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
     * 或对象形式 {"type":"function","function":{"name":"..."}} 强制调用指定 tool。
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

    @Override
    public Class<StreamChatResponse> getResponseClass() {
        return StreamChatResponse.class;
    }

    @Override
    public String getPath() {
        return "chat/completions";
    }

    public static StreamChatRequest create(List<ChatRequestMessage> messages, String model) {
        StreamChatRequest chatRequest = new StreamChatRequest();
        chatRequest.setMessages(messages);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
