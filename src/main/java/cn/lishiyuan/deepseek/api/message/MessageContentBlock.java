package cn.lishiyuan.deepseek.api.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Messages API 的内容块（请求与响应共用）。
 *
 * <p>用一个扁平类承载所有可能的内容块字段，按 {@code type} 取用对应字段：
 * <ul>
 *   <li>{@code text}：{@code text}；</li>
 *   <li>{@code thinking}：{@code thinking} + {@code signature}；</li>
 *   <li>{@code server_tool_use}：{@code id} + {@code name} + {@code input} + {@code caller} + {@code kind}；</li>
 *   <li>{@code web_search_tool_result}：{@code tool_use_id} + {@code content} + {@code kind}；</li>
 *   <li>{@code web_fetch_tool_result}：{@code tool_use_id} + {@code content} + {@code kind} + {@code signature}；</li>
 *   <li>{@code refusal}：{@code category} + {@code explanation}；</li>
 *   <li>{@code tool_use} / {@code tool_result}：客户端工具调用与结果。</li>
 * </ul>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageContentBlock {

    /** 块类型：text / thinking / server_tool_use / web_search_tool_result / web_fetch_tool_result / refusal / tool_use / tool_result / image / document */
    @JsonProperty("type")
    private String type;

    /** text 块的文本 */
    @JsonProperty("text")
    private String text;

    /** thinking 块的思考内容 */
    @JsonProperty("thinking")
    private String thinking;

    /** thinking / web_fetch_tool_result 块的签名（回传时必须原样保留） */
    @JsonProperty("signature")
    private String signature;

    /** server_tool_use / tool_use 的 id */
    @JsonProperty("id")
    private String id;

    /** server_tool_use / tool_use 的工具名 */
    @JsonProperty("name")
    private String name;

    /** server_tool_use / tool_use 的入参 */
    @JsonProperty("input")
    private Object input;

    /** server_tool_use 的 caller（如 {"type":"direct"}） */
    @JsonProperty("caller")
    private Object caller;

    /** 能力类型：search / deep_search / read_link / image_search / deep_image_search */
    @JsonProperty("kind")
    private String kind;

    /** 结果块关联的调用 id */
    @JsonProperty("tool_use_id")
    private String toolUseId;

    /** 结果块的内容：搜索结果数组或 web_fetch_result 对象 */
    @JsonProperty("content")
    private Object content;

    /** text 块的引用（使用联网工具时返回） */
    @JsonProperty("citations")
    private List<Object> citations;

    /** 请求侧图片/文档块的来源 */
    @JsonProperty("source")
    private Object source;

    /** refusal 块：恒为 null */
    @JsonProperty("category")
    private String category;

    /** refusal 块：请求中的图片被拦截时指出是哪张图，其余为 null */
    @JsonProperty("explanation")
    private String explanation;

    public MessageContentBlock() {
    }

    private MessageContentBlock(String type) {
        this.type = type;
    }

    public static MessageContentBlock text(String text) {
        MessageContentBlock block = new MessageContentBlock("text");
        block.text = text;
        return block;
    }

    /** 文档块：引用 Files API 上传的文件。 */
    public static MessageContentBlock document(String fileId) {
        MessageContentBlock block = new MessageContentBlock("document");
        block.source = java.util.Map.of("type", "file", "file_id", fileId);
        return block;
    }
}
