package cn.lishiyuan.deepseek.api;

/**
 * 请求携带非 JSON 原始请求体的标记接口（如 multipart/form-data 上传文件）。
 * <p>{@link DefaultClient} 在构建 HTTP 请求时检测到该接口，则使用 {@link #body()} 与
 * {@link #contentType()} 替换默认的 JSON 序列化路径。
 */
public interface MultipartBodyRequest {

    /** 原始请求体字节，需与 {@link #contentType()} 中的 boundary 保持一致。 */
    byte[] body();

    /** 请求体 Content-Type，如 {@code multipart/form-data; boundary=<boundary>}。 */
    String contentType();
}
