package com.bigbrotherlee.deepseek.request;

import com.alibaba.fastjson2.annotation.JSONField;
import com.bigbrotherlee.deepseek.enums.RoleEnums;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FimRequest {
    @JSONField(name = "model")
    private String model;

    @JSONField(name = "prompt")
    private String prompt;

    @JSONField(name = "echo")
    private Boolean echo;

    @JSONField(name = "frequency_penalty")
    private Double frequencyPenalty;

    @JSONField(name = "logprobs")
    private Integer logprobs;

    @JSONField(name = "max_tokens")
    private Integer maxTokens;

    @JSONField(name = "presence_penalty")
    private Double presencePenalty;

    @JSONField(name = "stop")
    private List<String> stop; // 可以是 String 或 List<String>

    @JSONField(name = "stream")
    private Boolean stream;

    @JSONField(name = "stream_options")
    private StreamOptions streamOptions;

    @JSONField(name = "include_usage")
    private Boolean includeUsage;

    @JSONField(name = "suffix")
    private String suffix;

    @JSONField(name = "temperature")
    private Double temperature;

    @JSONField(name = "top_p")
    private Double topP;

    @Data
    public static class StreamOptions {
        @JSONField(name = "include_usage")
        private Boolean includeUsage;
    }

    public static FimRequest create(String prompt, String model) {
        FimRequest chatRequest = new FimRequest();
        chatRequest.setPrompt(prompt);
        chatRequest.setModel(model);
        return chatRequest;
    }

}
