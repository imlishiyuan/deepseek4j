package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.common.Usage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse extends BaseResponse {
    @JsonProperty("id")
    private String id;
    @JsonProperty("created")
    private Integer created;
    @JsonProperty("model")
    private String model;
    @JsonProperty("choices")
    private List<Choice> choices;

    @JsonProperty("system_fingerprint")
    private String systemFingerprint;
    // chat.completion

    @JsonProperty("object")
    private String object;

    @JsonProperty("usage")
    private Usage usage;
}
