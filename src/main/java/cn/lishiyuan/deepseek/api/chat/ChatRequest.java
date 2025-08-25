package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.config.enums.ResponseFormatEnums;
import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion
 */
@Data
public class ChatRequest extends BaseRequest<ChatResponse> {

    @JSONField(name = "messages")
    private List<ChatRequestMessage> messages;

    // deepseek-chat, deepseek-reasoner
    @JSONField(name = "model")
    private String model = ModelEnums.DEEPSEEK_CHAT.code;
    /**
     * 介于 -2.0 和 2.0 之间的数字。如果该值为正，那么新 token 会根据其在已有文本中的出现频率受到相应的惩罚，降低模型重复相同内容的可能性。
     */
    @JSONField(name = "frequency_penalty")
    private Double frequencyPenalty;

    @JSONField(name = "max_tokens")
    private Integer maxTokens;

    @JSONField(name ="presence_penalty")
    private Double presencePenalty;

    @JSONField(name = "response_format")
    private ResponseFormat responseFormat;

    @JSONField(name = "stop")
    private List<String> stop;

    @JSONField(name = "stream")
    private final boolean stream = false;

    @JSONField(name = "stream_options")
    private StreamOptions streamOptions;

    @JSONField(name = "temperature")
    private Double temperature;

    @JSONField(name = "top_p")
    private Double topP;

    @JSONField(name = "tools")
    private List<Tool> tools;

    @JSONField(name = "tool_choice")
    private ToolChoice toolChoice;

    @JSONField(name = "logprobs")
    private Boolean logprobs;

    @JSONField(name = "top_logprobs")
    private Integer topLogprobs;

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
        @JSONField(name = "type")
        private String type = ResponseFormatEnums.TEXT.code;
    }

    @Data
    public static class StreamOptions {
        @JSONField(name = "include_usage")
        private Boolean includeUsage;
    }

    @Data
    public static class Tool{
        // function
        @JSONField(name = "type")
        private String type = ToolTypeEnums.FUNCTION.code;

        @JSONField(name = "function")
        private Function function;
    }
    @Data
    public static class ToolChoice{
        @JSONField(name = "type")
        private String type = ToolTypeEnums.FUNCTION.code;
        @JSONField(name = "function")
        private Function function;
    }

    @Data
    private static class Function{
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "description")
        private String description;
        @JSONField(name = "parameters")
        private List<Parameters> parameters;

    }

    @Data
    private static class Parameters{
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "properties")
        private Map<String,Param> properties;
        @JSONField(name = "required")
        private List<String> required;
    }

    @Data
    private static class Param{
        @JSONField(name = "type")
        private String type;
        @JSONField(name = "description")
        private String description;
    }



    public static ChatRequest create(List<ChatRequestMessage> messages, String model) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(messages);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
