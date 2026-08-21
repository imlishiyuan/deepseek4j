package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseRequest;

/**
 * 删除文件请求（DELETE /files/{file_id}）。
 * <p>请通过 {@link cn.lishiyuan.deepseek.Client#delete(BaseRequest)} 发起 DELETE 请求。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
public class DeleteFileRequest extends BaseRequest<DeleteFileResponse> {

    private final String fileId;

    public DeleteFileRequest(String fileId) {
        this.fileId = fileId;
    }

    public static DeleteFileRequest create(String fileId) {
        return new DeleteFileRequest(fileId);
    }

    @Override
    public String getPath() {
        return "files/" + fileId;
    }

    @Override
    public Class<DeleteFileResponse> getResponseClass() {
        return DeleteFileResponse.class;
    }
}
