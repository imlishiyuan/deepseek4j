package cn.lishiyuan.deepseek.api.fim;

import cn.lishiyuan.deepseek.api.BaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FimRequest extends BaseRequest<FimResponse> {
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
    private final boolean stream = false;

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

    public static FimRequest create(String prompt, String model) {
        FimRequest chatRequest = new FimRequest();
        chatRequest.setPrompt(prompt);
        chatRequest.setModel(model);
        return chatRequest;
    }

    @Override
    public Class<FimResponse> getResponseClass() {
        return FimResponse.class;
    }

    @Override
    public String getPath() {
        return "beta/completions";
    }
}
