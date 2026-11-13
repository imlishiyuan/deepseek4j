package cn.lishiyuan.deepseek.api.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;

/**
 * Messages API 的内容块（请求与响应共用）。
 *
 * <p>按 {@code type} 取用对应字段：
 * <ul>
 *   <li>{@code text}：{@link #getText()}；</li>
 *   <li>{@code thinking}：{@link #getThinking()} + {@link #getSignature()}；</li>
 *   <li>{@code server_tool_use}：{@link #getId()} + {@link #getName()} + {@link #getInput()} + {@link #getCaller()} + {@link #getKind()}；</li>
 *   <li>{@code tool_use}（客户端工具）：{@link #getId()} + {@link #getName()} + {@link #getInput()}；</li>
 *   <li>{@code web_search_tool_result} / {@code web_fetch_tool_result}：{@link #getToolUseId()} + {@link #getContent()} + {@link #getKind()}；</li>
 *   <li>{@code tool_result}（客户端工具结果）：{@link #getToolUseId()} + {@link #getContent()}；</li>
 *   <li>{@code refusal}：{@link #getCategory()} + {@link #getExplanation()}；</li>
 *   <li>请求侧的 {@code image} / {@code document}：{@link #getSource()}。</li>
 * </ul>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageContentBlock {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 块类型：text / thinking / server_tool_use / tool_use / web_search_tool_result / web_fetch_tool_result / tool_result / refusal / image / document */
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

    /**
     * 调用入参，可用结构：
     * <ul>
     *   <li>{@code server_tool_use}：{@code {"action":"search|open_page|find_in_page|read_link","queries"|"url"|"pattern"}}，
     *       用 {@link #serverToolInput()} 转成结构化对象；</li>
     *   <li>{@code tool_use}（客户端工具）：模型生成的**任意 JSON**（如 {@code {"city":"上海"}}），
     *       结构由你在 {@link MessageTool#function} 里定义的 {@code input_schema} 决定。</li>
     * </ul>
     */
    @JsonProperty("input")
    private JsonNode input;

    /** server_tool_use 的 caller */
    @JsonProperty("caller")
    private Caller caller;

    /** 能力类型：search / deep_search / read_link / image_search / deep_image_search */
    @JsonProperty("kind")
    private String kind;

    /** 结果块关联的调用 id */
    @JsonProperty("tool_use_id")
    private String toolUseId;

    /**
     * 结果块内容，可用结构：
     * <ul>
     *   <li>{@code web_search_tool_result}：**数组**（搜索结果），用 {@link #searchResults()} 取；</li>
     *   <li>{@code web_fetch_tool_result}：**对象** {@code {type,url,content{...},published_at}}，用 {@link #fetchResult()} 取；</li>
     *   <li>{@code tool_result}：**字符串**或内容块数组（工具返回值，原样回传）。</li>
     * </ul>
     */
    @JsonProperty("content")
    private JsonNode content;

    /** text 块的引用（使用联网工具时返回） */
    @JsonProperty("citations")
    private List<Citation> citations;

    /** 请求侧图片 / 文档块的来源 */
    @JsonProperty("source")
    private Source source;

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
        block.source = Source.file(fileId);
        return block;
    }

    /** 图片块：base64 内联。 */
    public static MessageContentBlock imageBase64(String mediaType, String base64Data) {
        MessageContentBlock block = new MessageContentBlock("image");
        block.source = Source.base64(mediaType, base64Data);
        return block;
    }

    /** 客户端工具结果块（回传给模型）。{@code output} 可为字符串或内容块数组。 */
    public static MessageContentBlock toolResult(String toolUseId, Object output) {
        MessageContentBlock block = new MessageContentBlock("tool_result");
        block.toolUseId = toolUseId;
        block.content = MAPPER.valueToTree(output);
        return block;
    }

    /** 把 {@code server_tool_use} 的入参转成结构化对象；非 server_tool_use 返回 null。 */
    public ServerToolInput serverToolInput() {
        if (input == null || !input.isObject()) {
            return null;
        }
        return MAPPER.convertValue(input, ServerToolInput.class);
    }

    /** {@code web_search_tool_result} 的搜索结果；非搜索结果为 null。 */
    public List<SearchResult> searchResults() {
        if (content == null || !content.isArray()) {
            return null;
        }
        return MAPPER.convertValue(content, new TypeReference<List<SearchResult>>() {
        });
    }

    /** {@code web_fetch_tool_result} 的抓取结果；非抓取结果为 null。 */
    public FetchResult fetchResult() {
        if (content == null || !content.isObject()) {
            return null;
        }
        return MAPPER.convertValue(content, FetchResult.class);
    }

    // ── 内部类型 ────────────────────────────────────────────────────────────

    /** {@code server_tool_use} 的入参：{@code {action, queries|url|pattern}}。 */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ServerToolInput {
        /** search / open_page / find_in_page / read_link */
        @JsonProperty("action")
        private String action;
        @JsonProperty("queries")
        private List<String> queries;
        @JsonProperty("url")
        private String url;
        @JsonProperty("pattern")
        private String pattern;
    }

    /** 服务端工具的 caller，目前为 {@code {"type":"direct"}}。 */
    @Data
    public static class Caller {
        @JsonProperty("type")
        private String type;
    }

    /** 回答正文里的引用来源。 */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Citation {
        /** 恒为 web_search_result_location */
        @JsonProperty("type")
        private String type;
        @JsonProperty("url")
        private String url;
        /** 来源标题，未知时为 null */
        @JsonProperty("title")
        private String title;
        /** 恒为空字符串 */
        @JsonProperty("encrypted_index")
        private String encryptedIndex;
        /** 当前文本块截至该引用处的回答文本 */
        @JsonProperty("cited_text")
        private String citedText;
    }

    /** 请求侧图片 / 文档的来源（三选一：file_id / base64 data / url）。 */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Source {
        /** text / file / base64 / url */
        @JsonProperty("type")
        private String type;
        @JsonProperty("media_type")
        private String mediaType;
        @JsonProperty("data")
        private String data;
        @JsonProperty("file_id")
        private String fileId;
        @JsonProperty("url")
        private String url;

        public static Source file(String fileId) {
            Source source = new Source();
            source.type = "file";
            source.fileId = fileId;
            return source;
        }

        public static Source base64(String mediaType, String data) {
            Source source = new Source();
            source.type = "base64";
            source.mediaType = mediaType;
            source.data = data;
            return source;
        }

        public static Source url(String url) {
            Source source = new Source();
            source.type = "url";
            source.url = url;
            return source;
        }
    }

    /** 一条搜索结果（{@code web_search_tool_result}）。 */
    @Data
    public static class SearchResult {
        @JsonProperty("type")
        private String type;
        @JsonProperty("title")
        private String title;
        @JsonProperty("url")
        private String url;
        @JsonProperty("encrypted_content")
        private String encryptedContent;
        @JsonProperty("page_age")
        private String pageAge;
        @JsonProperty("snippet")
        private String snippet;
        @JsonProperty("site_name")
        private String siteName;
        @JsonProperty("site_icon")
        private String siteIcon;
        @JsonProperty("image_url")
        private String imageUrl;
    }

    /** 抓取结果（{@code web_fetch_tool_result}）。 */
    @Data
    public static class FetchResult {
        @JsonProperty("type")
        private String type;
        @JsonProperty("url")
        private String url;
        @JsonProperty("content")
        private Document content;
        @JsonProperty("published_at")
        private String publishedAt;
    }

    /** 抓取结果的文档体。 */
    @Data
    public static class Document {
        @JsonProperty("type")
        private String type;
        @JsonProperty("source")
        private Source source;
        @JsonProperty("title")
        private String title;
    }
}
