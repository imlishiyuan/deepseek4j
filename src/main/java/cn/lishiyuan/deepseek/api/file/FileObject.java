package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 一个已上传的文件（OpenAI 兼容形态）。
 * <p>只有上传时设置了有效期，{@code expires_at} 才会出现。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
@Data
public class FileObject extends BaseResponse {

    /** 文件 id，形如 file-api-xxxxxxxxxxxxxxxx */
    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    /** 文件大小（字节） */
    @JsonProperty("bytes")
    private Long bytes;

    /** 创建时间（Unix 秒时间戳） */
    @JsonProperty("created_at")
    private Long createdAt;

    @JsonProperty("filename")
    private String filename;

    /** 用途，当前仅 user_data */
    @JsonProperty("purpose")
    private String purpose;

    /** 过期时间（Unix 秒时间戳），仅在设置有效期时出现 */
    @JsonProperty("expires_at")
    private Long expiresAt;
}
