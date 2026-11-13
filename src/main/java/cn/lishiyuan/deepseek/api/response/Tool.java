package cn.lishiyuan.deepseek.api.response;

import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Responses API 工具定义（扁平结构：name/description/parameters 直接在工具上）。
 * 与 {@code cn.lishiyuan.deepseek.api.chat.Tool}（嵌套 function 结构）不同。
 */
@Data
public class Tool {
    /** function / web_search / web_search_2025_08_26 */
    @JsonProperty("type")
    private String type = ToolTypeEnums.FUNCTION.code;

    /** function 必填：函数名，匹配 ^[a-zA-Z0-9_-]+$ 且 ≤128 字符 */
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    /**
     * JSON Schema 对象，描述函数参数。省略则定义空参数列表。
     * <p>键为参数名，值为该参数的 JSON Schema 片段
     * （如 {@code {"type":"string","description":"城市名"}}；可嵌套 object / array）。
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /** 服务端联网搜索工具（deepseek_web_search；Responses 下只传 type，不带 name）。 */
    public static Tool webSearch() {
        Tool tool = new Tool();
        tool.setType(ToolTypeEnums.DEEPSEEK_WEB_SEARCH.code);
        return tool;
    }

    /** 服务端阅读链接工具（deepseek_read_url）。 */
    public static Tool readUrl() {
        Tool tool = new Tool();
        tool.setType(ToolTypeEnums.DEEPSEEK_READ_URL.code);
        return tool;
    }
}
