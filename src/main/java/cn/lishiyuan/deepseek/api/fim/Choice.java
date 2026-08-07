package cn.lishiyuan.deepseek.api.fim;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Choice {
    @JsonProperty("finish_reason")
    private String finishReason;

    @JsonProperty("index")
    private Integer index;

    @JsonProperty("text")
    private String text;

    @JsonProperty("logprobs")
    private Logprobs logprobs;
}
