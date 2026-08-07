package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TokenLogprob {
    @JsonProperty("token")
    private String token;

    @JsonProperty("logprob")
    private Double logprob;

    @JsonProperty("bytes")
    private List<Integer> bytes;

    @JsonProperty("top_logprobs")
    private List<TokenLogprob> topLogprobs;
}
