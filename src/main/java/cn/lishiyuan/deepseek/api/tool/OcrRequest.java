package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.MultipartBodyRequest;
import cn.lishiyuan.deepseek.api.file.MultipartFormData;
import cn.lishiyuan.deepseek.config.enums.FilePurposeEnums;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * OCR 请求（{@code POST /v1/tools/ocr}，multipart/form-data）。
 *
 * <p>识别 PDF、Word、PowerPoint 文档或图片中的文字，按页返回识别结果；文件不会存入 Files API。
 * 大小限制、错误与计费见 OCR 指南。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/ocr">OCR API</a>
 */
@Getter
@Setter
public class OcrRequest extends BaseRequest<OcrResponse> implements MultipartBodyRequest {

    /** 要识别的文件内容，需同时提供文件名。 */
    @JsonIgnore
    private byte[] fileData;

    /** 文件名（用于判断文件类型）。 */
    @JsonIgnore
    private String filename;

    /**
     * 识别方式：{@code text_recognition}（默认，只提取文字）/ {@code rich_layout_recognition}（文字 + 图片版式）。
     * OCR 不支持 {@code user_data}。
     */
    @JsonIgnore
    private String purpose = FilePurposeEnums.TEXT_RECOGNITION.code;

    /** 文件部分的 Content-Type（格式由文件实际内容判断，此字段可省略）。 */
    @JsonIgnore
    private String fileContentType = "application/octet-stream";

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private byte[] cachedBody;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String cachedContentType;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private boolean built;

    public OcrRequest() {
    }

    public OcrRequest(byte[] fileData, String filename) {
        this.fileData = fileData;
        this.filename = filename;
    }

    /** 默认按 text_recognition 识别。 */
    public static OcrRequest create(byte[] fileData, String filename) {
        return new OcrRequest(fileData, filename);
    }

    /** 设置识别方式（见 {@link FilePurposeEnums}，OCR 只支持 text_recognition / rich_layout_recognition）。 */
    public OcrRequest purpose(String purpose) {
        this.purpose = purpose;
        this.built = false;
        return this;
    }

    /** 设置识别方式枚举。 */
    public OcrRequest purpose(FilePurposeEnums purpose) {
        return purpose(purpose.code);
    }

    /** 设置文件部分的 Content-Type。 */
    public OcrRequest fileContentType(String contentType) {
        this.fileContentType = contentType;
        this.built = false;
        return this;
    }

    private void ensureBuilt() {
        if (built) {
            return;
        }
        MultipartFormData mf = new MultipartFormData();
        // purpose 放在 file 之前，避免服务端按顺序解析时拿不到
        mf.addField("purpose", purpose);
        mf.addFile("file", filename != null ? filename : "file", fileContentType, fileData);
        cachedBody = mf.build();
        cachedContentType = mf.contentType();
        built = true;
    }

    @Override
    public byte[] body() {
        ensureBuilt();
        return cachedBody;
    }

    @Override
    public String contentType() {
        ensureBuilt();
        return cachedContentType;
    }

    @Override
    public String getPath() {
        return "v1/tools/ocr";
    }

    @Override
    public Class<OcrResponse> getResponseClass() {
        return OcrResponse.class;
    }
}
