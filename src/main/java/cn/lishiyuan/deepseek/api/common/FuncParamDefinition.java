package cn.lishiyuan.deepseek.api.common;

import cn.lishiyuan.deepseek.config.enums.FuncParamEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FuncParamDefinition {
    @JsonProperty("type")
    private String type;
    @JsonProperty("description")
    private String description;

    // 字符串使用
    // 正则表达式
    @JsonProperty("pattern")
    private String pattern;
    // 字符串使用
    // 预定义格式
    /**
     * email：电子邮件地址
     *     hostname：主机名
     *     ipv4：IPv4 地址
     *     ipv6：IPv6 地址
     *     uuid：uuid
     */
    @JsonProperty("format")
    private String format;

    // 数字使用
    // 常数
    @JsonProperty("const")
    private Number consts;
    // 数字使用
    // 默认值
    @JsonProperty("default")
    private Number defaults;
    // 数字使用
    // 最小
    @JsonProperty("minimum")
    private Number minimum;
    // 数字使用
    // 最大
    @JsonProperty("maximum")
    private Number maximum;
    // 数字使用
    // 不小于
    @JsonProperty("exclusiveMinimum")
    private Number exclusiveMinimum;
    // 数字使用
    // 不大于
    @JsonProperty("exclusiveMaximum")
    private Number exclusiveMaximum;
    // 数字使用
    // 此数的倍数
    @JsonProperty("multipleOf")
    private Number multipleOf;
    // enum时候使用
    @JsonProperty("enum")
    private List<Object> enums;
    // array时候使用
    @JsonProperty("items")
    private FuncParamEnums items;

}
