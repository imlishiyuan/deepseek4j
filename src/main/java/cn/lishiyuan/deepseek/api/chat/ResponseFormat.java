package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.ResponseFormatEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResponseFormat {
    @JsonProperty("type")
    private String type = ResponseFormatEnums.TEXT.code;
}
