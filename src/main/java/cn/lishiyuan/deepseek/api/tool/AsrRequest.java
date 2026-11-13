package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.MultipartBodyRequest;
import cn.lishiyuan.deepseek.api.file.MultipartFormData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 语音识别请求（{@code POST /v1/tools/asr}，multipart/form-data）。
 *
 * <p>把音频文件识别为文本。整段音频在一次请求中上传，返回完整的识别结果。
 * 音频格式：16 位 PCM WAV 或 Ogg Opus。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/asr">语音识别 API</a>
 */
@Getter
@Setter
public class AsrRequest extends BaseRequest<AsrResponse> implements MultipartBodyRequest {

    /** 音频文件内容：16 位 PCM WAV 或 Ogg Opus。 */
    @JsonIgnore
    private byte[] fileData;

    /** 文件名（用于判断音频类型）。 */
    @JsonIgnore
    private String filename;

    /** 文件部分的 Content-Type，默认 audio/wav（Ogg Opus 用 audio/ogg）。 */
    @JsonIgnore
    private String fileContentType = "audio/wav";

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

    public AsrRequest() {
    }

    public AsrRequest(byte[] fileData, String filename) {
        this.fileData = fileData;
        this.filename = filename;
    }

    public static AsrRequest create(byte[] fileData, String filename) {
        return new AsrRequest(fileData, filename);
    }

    /** Ogg Opus 音频用 audio/ogg。 */
    public AsrRequest oggOpus() {
        this.fileContentType = "audio/ogg";
        this.built = false;
        return this;
    }

    /** 设置文件部分的 Content-Type。 */
    public AsrRequest fileContentType(String contentType) {
        this.fileContentType = contentType;
        this.built = false;
        return this;
    }

    private void ensureBuilt() {
        if (built) {
            return;
        }
        MultipartFormData mf = new MultipartFormData();
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
        return "v1/tools/asr";
    }

    @Override
    public Class<AsrResponse> getResponseClass() {
        return AsrResponse.class;
    }
}
