package cn.lishiyuan.deepseek.api.fim;

import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StreamFimRequest extends BaseStreamRequest<StreamFimResponse> {
    @JsonProperty("model")
    private String model;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("echo")
    private Boolean echo;

    /**
     * @deprecated 已废弃，传入无效果。见 API 文档。
     */
    @Deprecated
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @JsonProperty("logprobs")
    private Integer logprobs;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * @deprecated 已废弃，传入无效果。见 API 文档。
     */
    @Deprecated
    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("stop")
    private List<String> stop; // 可以是 String 或 List<String>

    @JsonProperty("stream")
    private final boolean stream = true;

    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    @JsonProperty("suffix")
    private String suffix;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @Data
    public static class StreamOptions {
        @JsonProperty("include_usage")
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
