package cn.lishiyuan.deepseek.api.fim;

import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.chat.StreamChatResponse;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class StreamFimRequest extends BaseStreamRequest<StreamFimResponse> {
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
    private final boolean stream = true;

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

    public static StreamFimRequest create(String prompt, String model) {
        StreamFimRequest chatRequest = new StreamFimRequest();
        chatRequest.setPrompt(prompt);
        chatRequest.setModel(model);
        return chatRequest;
    }

    @Override
    public String getPath() {
        return "beta/completions";
    }

    @Override
    public Class<StreamFimResponse> getResponseClass() {
        return StreamFimResponse.class;
    }
}
