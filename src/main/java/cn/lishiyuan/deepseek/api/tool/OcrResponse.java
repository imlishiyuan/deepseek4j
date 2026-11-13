package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * OCR 响应：按页返回识别结果。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/ocr">OCR API</a>
 */
@Data
public class OcrResponse extends BaseResponse {

    /** 本次识别的 ID，形如 ocr-... */
    @JsonProperty("id")
    private String id;

    /** 对象类型，固定为 ocr.result */
    @JsonProperty("object")
    private String object;

    /** 识别创建时的 Unix 时间戳（秒） */
    @JsonProperty("created")
    private Integer created;

    /** 请求中的 purpose */
    @JsonProperty("purpose")
    private String purpose;

    /** 页数，等于 pages 的长度 */
    @JsonProperty("page_count")
    private Integer pageCount;

    /** 每一页的识别结果，按页序排列 */
    @JsonProperty("pages")
    private List<Page> pages;

    /** 所有页的 Token 用量，仅供参考，不用于计费 */
    @JsonProperty("usage")
    private OcrUsage usage;

    /** 单页识别结果。 */
    @Data
    public static class Page {
        /** 页码，从 0 开始 */
        @JsonProperty("index")
        private Integer index;

        /** 该页的模型原始输出，不做任何清理 */
        @JsonProperty("text")
        private String text;

        /** stop 表示完整识别；length 表示输出达到上限，该页文本不完整 */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /** OCR 的 Token 用量（与对话的 usage 结构不同）。 */
    @Data
    public static class OcrUsage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;

        @JsonProperty("output_tokens")
        private Integer outputTokens;

        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
