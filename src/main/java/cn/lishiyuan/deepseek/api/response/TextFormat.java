package cn.lishiyuan.deepseek.api.response;

import cn.lishiyuan.deepseek.config.enums.ResponseTextFormatEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class TextFormat {
    /** text / json_object / json_schema */
    @JsonProperty("type")
    private String type = ResponseTextFormatEnums.TEXT.code;

    /** type=json_schema 时必填：schema 名称 */
    @JsonProperty("name")
    private String name;

    /**
     * {@code type=json_schema} 时必填：JSON Schema 对象。
     * <p>键为属性名，值为该属性的 JSON Schema 片段（如 {@code {"type":"string"}}）。
     */
    @JsonProperty("schema")
    private Map<String, Object> schema;
}
