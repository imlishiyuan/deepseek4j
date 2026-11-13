package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 工具引用：用于请求的 tools 数组，也用于 tool_choice 对象形式。
 *
 * <ul>
 *   <li>function 工具：{@code {"type":"function","function":{...}}}；</li>
 *   <li>服务端工具（DeepSeek 扩展）：{@code {"type":"deepseek_web_search","name":"deepseek_web_search"}}
 *       与 {@code {"type":"deepseek_read_url","name":"deepseek_read_url"}}，由服务端执行，
 *       用 {@link #webSearch()} / {@link #readUrl()} 构造。</li>
 * </ul>
 */
@Data
public class Tool {
    // function / deepseek_web_search / deepseek_read_url
    @JsonProperty("type")
    private String type = ToolTypeEnums.FUNCTION.code;

    @JsonProperty("function")
    private ToolFunction function;

    /**
     * 服务端工具名（DeepSeek 扩展字段）。
     * 仅 {@code deepseek_web_search} / {@code deepseek_read_url} 使用；function 工具为 null，不会被序列化。
     */
    @JsonProperty("name")
    private String name;

    /** 声明服务端联网搜索工具（{@code deepseek_web_search}）。 */
    public static Tool webSearch() {
        return serverTool(ToolTypeEnums.DEEPSEEK_WEB_SEARCH.code);
    }

    /** 声明服务端阅读链接工具（{@code deepseek_read_url}）。 */
    public static Tool readUrl() {
        return serverTool(ToolTypeEnums.DEEPSEEK_READ_URL.code);
    }

    private static Tool serverTool(String code) {
        Tool tool = new Tool();
        tool.setType(code);
        tool.setName(code);
        return tool;
    }
}
