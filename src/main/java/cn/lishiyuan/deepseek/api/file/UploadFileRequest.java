package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.MultipartBodyRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * 上传文件请求（multipart/form-data）。
 * <p>通过 {@code POST /files} 上传图片，成功后返回 {@link FileObject}，之后再以
 * {@code file_id} 在多模态消息中引用（见 {@link cn.lishiyuan.deepseek.api.chat.msg.content.ContentPart#file(String)}）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
@Getter
@Setter
public class UploadFileRequest extends BaseRequest<FileObject> implements MultipartBodyRequest {

    /** 要上传的图片文件字节。 */
    @JsonIgnore
    private byte[] fileData;

    /** 文件名（最多 512 字符）。 */
    @JsonIgnore
    private String filename;

    /** 用途，必须为 user_data。 */
    @JsonIgnore
    private String purpose = "user_data";

    /** 有效期（秒），取值 3600 到 2592000（1 小时到 30 天）；不设置则文件永久有效。 */
    @JsonIgnore
    private Integer expiresAfterSeconds;

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

    public UploadFileRequest() {
    }

    public UploadFileRequest(byte[] fileData, String filename) {
        this.fileData = fileData;
        this.filename = filename;
    }

    /**
     * 上传文件（默认 purpose=user_data，永久有效）。
     */
    public static UploadFileRequest create(byte[] fileData, String filename) {
        return new UploadFileRequest(fileData, filename);
    }

    /** 设置有效期（秒），需在 3600 到 2592000 之间；并自动设置 expires_after[anchor]=created_at。 */
    public UploadFileRequest expiresAfterSeconds(int seconds) {
        this.expiresAfterSeconds = seconds;
        return this;
    }

    /** 设置文件部分的 Content-Type。 */
    public UploadFileRequest fileContentType(String contentType) {
        this.fileContentType = contentType;
        return this;
    }

    private void ensureBuilt() {
        if (built) {
            return;
        }
        MultipartFormData mf = new MultipartFormData();
        mf.addField("purpose", purpose);
        if (expiresAfterSeconds != null) {
            mf.addField("expires_after[anchor]", "created_at");
            mf.addField("expires_after[seconds]", String.valueOf(expiresAfterSeconds));
        }
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
        return "files";
    }

    @Override
    public Class<FileObject> getResponseClass() {
        return FileObject.class;
    }
}
