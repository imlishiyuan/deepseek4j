package cn.lishiyuan.deepseek.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 内容块：input_text / output_text / reasoning_text。
 * 输入（InputItem.content）与输出（OutputItem.content）结构一致，共用此类型。
 */
@Data
public class ContentBlock {
    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private String text;
}
