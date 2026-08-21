package cn.lishiyuan.deepseek.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 内容块：input_text / output_text / reasoning_text / input_image。
 * 输入（InputItem.content / function_call_output.output）与输出（OutputItem.content）结构一致，共用此类型。
 *
 * <p>多模态时支持 {@code input_image} 内容块，承载图片（见
 * <a href="https://api-docs.deepseek.com/zh-cn/guides/responses_api/">Responses API 指南</a>）：
 * <ul>
 *   <li>{@code image_url}：http(s) 链接（最多 8192 字符）或 base64 data URL；</li>
 *   <li>{@code file_id}：通过 Files API 上传的图片文件 ID（{@code file-api-...}）；</li>
 *   <li>{@code detail}：low / high / original / auto，设置 file_id 时被忽略。</li>
 * </ul>
 * {@code image_url} 与 {@code file_id} 互斥：都不传或都传返回 400。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBlock {
    @JsonProperty("type")
    private String type;

    /** input_text / output_text / reasoning_text 的文本内容 */
    @JsonProperty("text")
    private String text;

    /** input_image 的图片地址（http(s) URL 或 base64 data URL） */
    @JsonProperty("image_url")
    private String imageUrl;

    /** input_image 的 Files API 文件 ID（与 image_url 互斥） */
    @JsonProperty("file_id")
    private String fileId;

    /** input_image 的图片处理细节：low / high / original / auto（设置 file_id 时忽略） */
    @JsonProperty("detail")
    private String detail;

    public ContentBlock() {
    }

    private ContentBlock(String type) {
        this.type = type;
    }

    /** 文本内容块（input_text / output_text / reasoning_text）。 */
    public static ContentBlock text(String type, String text) {
        ContentBlock block = new ContentBlock(type);
        block.text = text;
        return block;
    }

    /** 图片内容块（input_image），使用外部 http(s) 链接或 base64 data URL。 */
    public static ContentBlock inputImage(String imageUrl) {
        ContentBlock block = new ContentBlock("input_image");
        block.imageUrl = imageUrl;
        return block;
    }

    /** 图片内容块（input_image），使用外部链接或 data URL，并指定 detail。 */
    public static ContentBlock inputImage(String imageUrl, String detail) {
        ContentBlock block = inputImage(imageUrl);
        block.detail = detail;
        return block;
    }

    /** 图片内容块（input_image），引用 Files API 的 file_id。 */
    public static ContentBlock inputImageByFileId(String fileId) {
        ContentBlock block = new ContentBlock("input_image");
        block.fileId = fileId;
        return block;
    }
}
