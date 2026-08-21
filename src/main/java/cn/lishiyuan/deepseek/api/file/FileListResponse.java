package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 列出文件的响应（分页列表）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
@Data
public class FileListResponse extends BaseResponse {

    /** 恒为 list */
    @JsonProperty("object")
    private String object;

    @JsonProperty("data")
    private List<FileObject> data;

    /** 分页游标：当前页第一条文件的 id */
    @JsonProperty("first_id")
    private String firstId;

    /** 分页游标：当前页最后一条文件的 id */
    @JsonProperty("last_id")
    private String lastId;

    /** 是否还有更多数据 */
    @JsonProperty("has_more")
    private Boolean hasMore;
}
