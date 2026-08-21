package cn.lishiyuan.deepseek.api.chat.msg.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * {@code image_url} 内容块的 image_url 字段。
 * <p>image 可通过外部 URL、base64 data URL 传入，亦可选填 {@code detail} 控制图像处理粒度：
 * <ul>
 *   <li>low：推理前缩放到 512×512；</li>
 *   <li>high / original：保留原图（为兼容性提供，含义等价）；</li>
 *   <li>auto：自动选择（当前等价于 original）。</li>
 * </ul>
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/vision/">图像理解</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageUrl {

    /**
     * 图片地址：http(s) 外部链接（最多 8192 字符），或 data:image/...;base64,<BASE64_DATA> 内联地址。
     */
    @JsonProperty("url")
    private String url;

    /**
     * 图片处理细节级别：low / high / original / auto。
     */
    @JsonProperty("detail")
    private String detail;

    public ImageUrl() {
    }

    public ImageUrl(String url) {
        this.url = url;
    }

    /**
     * 使用外部 http(s) 图片链接。
     */
    public static ImageUrl ofUrl(String url) {
        return new ImageUrl(url);
    }

    /**
     * 使用 base64 data URL 内联图片。
     *
     * @param mediaType 图片类型，如 {@code image/jpeg}、{@code image/png}、{@code image/gif}、{@code image/webp}
     * @param base64Data base64 编码后的图片数据（不含 data: 前缀与 base64, 分隔符）
     */
    public static ImageUrl dataUrl(String mediaType, String base64Data) {
        return new ImageUrl("data:" + mediaType + ";base64," + base64Data);
    }
}
