package cn.lishiyuan.deepseek.api.fim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Logprobs {
    @JsonProperty("text_offset")
    private List<Integer> textOffset;

    @JsonProperty("token_logprobs")
    private List<Double> tokenLogprobs;

    private List<String> tokens;

    @JsonProperty("top_logprobs")
    private List<Object> topLogprobs;
}
