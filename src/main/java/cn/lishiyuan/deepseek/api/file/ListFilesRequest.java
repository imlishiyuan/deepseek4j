package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 列出文件请求（GET /files）。
 * <p>支持分页与过滤参数：{@code after}（分页游标）、{@code limit}（1-1000）、{@code order}（asc/desc）、{@code purpose}（仅 user_data）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
@Getter
@Setter
public class ListFilesRequest extends BaseRequest<FileListResponse> {

    /** 分页游标：返回此 file_id 之后的文件。 */
    private String after;

    /** 返回数量，取值 1-1000。 */
    private Integer limit;

    /** 排序：asc（默认）或 desc。 */
    private String order;

    /** 按用途过滤，仅支持 user_data。 */
    private String purpose;

    public static ListFilesRequest create() {
        return new ListFilesRequest();
    }

    @Override
    public String getPath() {
        StringBuilder sb = new StringBuilder("files");
        StringBuilder query = new StringBuilder();
        appendQuery(query, "after", after);
        appendQuery(query, "limit", limit == null ? null : limit.toString());
        appendQuery(query, "order", order);
        appendQuery(query, "purpose", purpose);
        if (query.length() > 0) {
            sb.append('?').append(query);
        }
        return sb.toString();
    }

    private static void appendQuery(StringBuilder query, String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (query.length() > 0) {
            query.append('&');
        }
        query.append(name).append('=').append(value);
    }

    @Override
    public Class<FileListResponse> getResponseClass() {
        return FileListResponse.class;
    }
}
