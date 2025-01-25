package com.bigbrotherlee.deepseek.request;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bigbrotherlee.deepseek.enums.RoleEnums;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    @JSONField(name = "messages")
    private List<Message> messages;
    // deepseek-chat, deepseek-reasoner
    @JSONField(name = "model")
    private String model;

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
    private Boolean stream;

    @JSONField(name = "stream_options")
    private StreamOptions streamOptions;

    @JSONField(name = "temperature")
    private Double temperature;

    @JSONField(name = "top_p")
    private Double topP;

//    @JSONField(name = "tools")
//    private List<Tool> tools;
//
//    @JSONField(name = "tool_choice")
//    private ToolChoice toolChoice;

    @JSONField(name = "logprobs")
    private Boolean logprobs;

    @JSONField(name = "top_logprobs")
    private Integer topLogprobs;



    @Data
    @Accessors(chain = true)
    public static class Message {
        private String role;
        private String content;
    }


    @Data
    public static class ResponseFormat {
        // text, json_object
        private String type;
    }

    @Data
    public static class StreamOptions {
        @JSONField(name = "include_usage")
        private Boolean includeUsage;
    }

    @Data
    public static class Tool{
        // function
        private String type;
        private Function function;
    }
    @Data
    public static class ToolChoice{
        private String type;
        private String name;
    }

    @Data
    private static class Function{
        private String name;
        private String description;
        private Map<String,Object> parameters;
    }

    public static ChatRequest create(List<Message> messages, String model) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(messages);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
