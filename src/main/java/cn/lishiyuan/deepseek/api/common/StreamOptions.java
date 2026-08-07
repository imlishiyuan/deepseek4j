package cn.lishiyuan.deepseek.api.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StreamOptions {
    @JsonProperty("include_usage")
    private Boolean includeUsage;
}
