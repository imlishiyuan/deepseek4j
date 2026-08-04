package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.config.enums.ResponseFormatEnums;
import cn.lishiyuan.deepseek.config.enums.ThinkingEnums;
import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion/
 */
@Data
public class ChatRequest extends BaseRequest<ChatResponse> {

    @JsonProperty("messages")
    private List<ChatRequestMessage> messages;

    // deepseek-v4-flash, deepseek-v4-pro
    @JsonProperty("model")
    private String model = ModelEnums.DEEPSEEK_V4_FLASH.code;
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
    public Class<ChatResponse> getResponseClass() {
        return ChatResponse.class;
    }

    @Override
    public String getPath() {
        return "chat/completions";
    }


    @Data
    public static class ResponseFormat {
        @JsonProperty("type")
        private String type = ResponseFormatEnums.TEXT.code;
    }

    @Data
    public static class StreamOptions {
        @JsonProperty("include_usage")
        private Boolean includeUsage;
    }

    @Data
    public static class Thinking{
        @JsonProperty("type")
        private String type = ThinkingEnums.DISABLED.code;
        /**
         * 推理强度，仅 type=enabled 时生效。可选 low/high/max。
         * 不设置时由服务端按默认值（high）处理。
         */
        @JsonProperty("reasoning_effort")
        private String reasoningEffort;
    }

    @Data
    public static class Tool{
        // function
        @JsonProperty("type")
        private String type = ToolTypeEnums.FUNCTION.code;

        @JsonProperty("function")
        private Function function;
    }

    @Data
    public static class ToolChoice{
        @JsonProperty("type")
        private String type = ToolTypeEnums.FUNCTION.code;
        @JsonProperty("function")
        private Function function;
    }

    @Data
    public static class Function{
        @JsonProperty("name")
        private String name;
        @JsonProperty("description")
        private String description;
        /**
         * JSON Schema 对象，描述函数参数。省略则定义空参数列表。
         */
        @JsonProperty("parameters")
        private Map<String, Object> parameters;
        /**
         * (Beta) 设为 true 确保输出符合 JSON Schema，默认 false。
         */
        @JsonProperty("strict")
        private Boolean strict;
    }



    public static ChatRequest create(List<ChatRequestMessage> messages, String model) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(messages);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
