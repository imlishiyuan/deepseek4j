package cn.lishiyuan.deepseek.api.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * multipart/form-data 请求体构造器（用于上传文件）。
 * <p>生成固定 boundary 的请求体字节，并保持 Content-Type 与 body 一致。
 */
public final class MultipartFormData {
    private final String boundary;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public MultipartFormData() {
        this.boundary = "----DeepSeek4jBoundary" + Long.toHexString(System.nanoTime());
    }

    public String boundary() {
        return boundary;
    }

    /** 追加一个普通表单字段。 */
    public MultipartFormData addField(String name, String value) {
        writeHeader(name, null, null);
        write(value + "\r\n");
        return this;
    }

    /** 追加一个文件字段。 */
    public MultipartFormData addFile(String name, String filename, String contentType, byte[] data) {
        writeHeader(name, filename, contentType);
        write(data);
        write("\r\n");
        return this;
    }

    public byte[] build() {
        write("--" + boundary + "--\r\n");
        return out.toByteArray();
    }

    /** 与 {@link #build()} 对应的 Content-Type。 */
    public String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    private void writeHeader(String name, String filename, String contentType) {
        write("--" + boundary + "\r\n");
        StringBuilder disposition = new StringBuilder("Content-Disposition: form-data; name=\"").append(name).append("\"");
        if (filename != null) {
            disposition.append("; filename=\"").append(filename).append("\"");
        }
        disposition.append("\r\n");
        write(disposition.toString());
        if (contentType != null) {
            write("Content-Type: " + contentType + "\r\n");
        }
        write("\r\n");
    }

    private void write(String data) {
        write(data.getBytes(StandardCharsets.UTF_8));
    }

    private void write(byte[] data) {
        try {
            out.write(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
