package cn.lishiyuan.deepseek.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Text {
    @JsonProperty("format")
    private TextFormat format;
}
