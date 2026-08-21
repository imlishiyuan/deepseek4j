package cn.lishiyuan.deepseek.api.file;

import cn.lishiyuan.deepseek.api.BaseRequest;

/**
 * 查询单个文件信息请求（GET /files/{file_id}）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/files_api/">Files API</a>
 */
public class RetrieveFileRequest extends BaseRequest<FileObject> {

    private final String fileId;

    public RetrieveFileRequest(String fileId) {
        this.fileId = fileId;
    }

    public static RetrieveFileRequest create(String fileId) {
        return new RetrieveFileRequest(fileId);
    }

    @Override
    public String getPath() {
        return "files/" + fileId;
    }

    @Override
    public Class<FileObject> getResponseClass() {
        return FileObject.class;
    }
}
