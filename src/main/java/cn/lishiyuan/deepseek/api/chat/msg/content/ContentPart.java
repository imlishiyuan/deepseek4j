package cn.lishiyuan.deepseek.api.chat.msg.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 多模态消息内容块（content block）。
 * <p>开启多模态后，消息的 {@code content} 不再只是纯字符串，而是内容块的数组：
 * 纯文本、图片（外部 URL 或 base64 data URL）、以及通过 Files API 上传的文件引用。
 * 仅 {@code user} 消息允许携带图片；{@code system}/{@code assistant} 消息携带图片返回 400。
 * <p>三种内容块（由 {@code type} 判别）：
 * <ul>
 *   <li>{@code text}：{@code text} 纯文本；</li>
 *   <li>{@code image_url}：{@code image_url} 图片（见 {@link ImageUrl}）；</li>
 *   <li>{@code file}：{@code file_id} 或 {@code file_data} 文件引用（二者互斥），可选 {@code filename}。</li>
 * </ul>
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/vision/">图像理解</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentPart {

    /** 内容块类型：text / image_url / file */
    @JsonProperty("type")
    private String type;

    /** text 块的文本内容 */
    @JsonProperty("text")
    private String text;

    /** image_url 块的图片地址信息 */
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    /** file 块的 file_id（与 file_data 互斥） */
    @JsonProperty("file_id")
    private String fileId;

    /** file 块的 base64 data URL 内联图片（与 file_id 互斥） */
    @JsonProperty("file_data")
    private String fileData;

    /** file 块的原始文件名（配合 file_data，不能与 file_id 同时出现） */
    @JsonProperty("filename")
    private String filename;

    public ContentPart() {
    }

    private ContentPart(String type) {
        this.type = type;
    }

    /**
     * 纯文本内容块。
     */
    public static ContentPart text(String text) {
        ContentPart part = new ContentPart("text");
        part.text = text;
        return part;
    }

    /**
     * 图片内容块，使用外部 http(s) 链接或 base64 data URL。
     */
    public static ContentPart imageUrl(ImageUrl imageUrl) {
        ContentPart part = new ContentPart("image_url");
        part.imageUrl = imageUrl;
        return part;
    }

    /**
     * 图片内容块，使用外部 http(s) 链接。
     */
    public static ContentPart imageUrl(String url) {
        return imageUrl(new ImageUrl(url));
    }

    /**
     * 图片内容块，使用 base64 data URL 内联图片。
     */
    public static ContentPart imageDataUrl(String mediaType, String base64Data) {
        return imageUrl(ImageUrl.dataUrl(mediaType, base64Data));
    }

    /**
     * 文件内容块，通过 Files API 上传后以其 file_id 引用。
     */
    public static ContentPart file(String fileId) {
        ContentPart part = new ContentPart("file");
        part.fileId = fileId;
        return part;
    }

    /**
     * 文件内容块，以 base64 data URL 内联携带图片（file_data），可额外指定原始文件名。
     */
    public static ContentPart fileData(String fileData, String filename) {
        ContentPart part = new ContentPart("file");
        part.fileData = fileData;
        part.filename = filename;
        return part;
    }
}
