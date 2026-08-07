package cn.lishiyuan.deepseek.api.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Logprobs {
    @JsonProperty("content")
    private List<TokenLogprob> content;
    @JsonProperty("reasoning_content")
    private List<TokenLogprob> reasoningContent;
}
