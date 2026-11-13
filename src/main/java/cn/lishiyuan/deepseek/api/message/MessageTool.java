package cn.lishiyuan.deepseek.api.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Messages API 的工具定义。
 *
 * <ul>
 *   <li>客户端工具：{@code {"name","description","input_schema"}}（用 {@link #function}）；</li>
 *   <li>服务端联网搜索：{@code {"type":"deepseek_web_search","name":"deepseek_web_search"}}（用 {@link #webSearch()}）；</li>
 *   <li>服务端阅读链接：{@code {"type":"deepseek_read_url","name":"deepseek_read_url"}}（用 {@link #readUrl()}）。</li>
 * </ul>
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/create-message">Messages API</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageTool {

    /** 工具类型：不传表示客户端工具；服务端工具为 deepseek_web_search / deepseek_read_url */
    @JsonProperty("type")
    private String type;

    /** 工具名 */
    @JsonProperty("name")
    private String name;

    /** 工具描述（客户端工具） */
    @JsonProperty("description")
    private String description;

    /**
     * JSON Schema：工具入参定义（客户端工具）。
     * <p>键为参数名，值为该参数的 JSON Schema 片段（如 {@code {"type":"string","description":"城市名"}}）。
     */
    @JsonProperty("input_schema")
    private Map<String, Object> inputSchema;

    /** 客户端工具。 */
    public static MessageTool function(String name, String description, Map<String, Object> inputSchema) {
        MessageTool tool = new MessageTool();
        tool.name = name;
        tool.description = description;
        tool.inputSchema = inputSchema;
        return tool;
    }

    /** 服务端联网搜索工具（deepseek_web_search）。 */
    public static MessageTool webSearch() {
        return serverTool("deepseek_web_search");
    }

    /** 服务端阅读链接工具（deepseek_read_url）。 */
    public static MessageTool readUrl() {
        return serverTool("deepseek_read_url");
    }

    private static MessageTool serverTool(String name) {
        MessageTool tool = new MessageTool();
        tool.type = name;
        tool.name = name;
        return tool;
    }
}
