package cn.lishiyuan.deepseek.api.chat;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FunctionParameter {
    private String type;
    private Map<String,Object> properties;
    private List<String> required;
    private Boolean additionalProperties = false;
}
