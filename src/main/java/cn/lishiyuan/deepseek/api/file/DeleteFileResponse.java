package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 删除文件的响应。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
@Data
public class DeleteFileResponse extends BaseResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("object")
    private String object;

    /** 是否删除成功 */
    @JsonProperty("deleted")
    private Boolean deleted;
}
