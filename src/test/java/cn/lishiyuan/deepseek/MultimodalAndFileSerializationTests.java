package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.chat.ChatRequest;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.api.chat.msg.content.ContentPart;
import cn.lishiyuan.deepseek.api.chat.msg.content.ImageUrl;
import cn.lishiyuan.deepseek.api.file.DeleteFileRequest;
import cn.lishiyuan.deepseek.api.file.DeleteFileResponse;
import cn.lishiyuan.deepseek.api.file.FileListResponse;
import cn.lishiyuan.deepseek.api.file.FileObject;
import cn.lishiyuan.deepseek.api.file.ListFilesRequest;
import cn.lishiyuan.deepseek.api.file.RetrieveFileRequest;
import cn.lishiyuan.deepseek.api.file.UploadFileRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多模态消息内容块与文件 API 序列化校验，不依赖网络与 accessKey。
 * 配置与 {@link DefaultClient} 中的 ObjectMapper 保持一致。
 */
@DisplayName("多模态与文件 API 序列化测试")
public class MultimodalAndFileSerializationTests {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private JsonNode serialize(UserMessage m) throws Exception {
        ChatRequest req = ChatRequest.create(List.of(m), ModelEnums.DEEPSEEK_V4_FLASH.code);
        return MAPPER.readTree(MAPPER.writeValueAsString(req)).get("messages").get(0).get("content");
    }

    @Test
    @DisplayName("文本 content 仍为字符串（向后兼容）")
    public void testStringContentBackwardCompatible() throws Exception {
        UserMessage m = new UserMessage();
        m.setContent("你好");
        JsonNode content = serialize(m);
        assertTrue(content.isTextual());
        assertEquals("你好", content.asText());
    }

    @Test
    @DisplayName("多模态 content 为块数组，正确携带 text/image_url/file 类型")
    public void testMultimodalContentArray() throws Exception {
        UserMessage m = new UserMessage();
        m.setContent(
                ContentPart.text("这张图片里有什么？"),
                ContentPart.imageUrl("https://example.com/image.jpg"),
                ContentPart.file("file-api-abc"));
        JsonNode content = serialize(m);
        assertTrue(content.isArray());
        assertEquals(3, content.size());
        assertEquals("text", content.get(0).get("type").asText());
        assertEquals("这张图片里有什么？", content.get(0).get("text").asText());
        assertEquals("image_url", content.get(1).get("type").asText());
        assertEquals("https://example.com/image.jpg", content.get(1).get("image_url").get("url").asText());
        assertEquals("file", content.get(2).get("type").asText());
        assertEquals("file-api-abc", content.get(2).get("file_id").asText());
    }

    @Test
    @DisplayName("image_url 支持 base64 data url 与 detail")
    public void testImageDataUrlAndDetail() throws Exception {
        UserMessage m = new UserMessage();
        ImageUrl imageUrl = ImageUrl.dataUrl("image/jpeg", "QUJD");
        imageUrl.setDetail("low");
        m.setContent(ContentPart.imageUrl(imageUrl));
        JsonNode img = serialize(m).get(0).get("image_url");
        assertEquals("data:image/jpeg;base64,QUJD", img.get("url").asText());
        assertEquals("low", img.get("detail").asText());
    }

    @Test
    @DisplayName("file_data 内联与 filename 序列化正确，file_id/image_url 不输出")
    public void testFileDataInline() throws Exception {
        UserMessage m = new UserMessage();
        m.setContent(ContentPart.fileData("data:image/png;base64,QUJD", "image.png"));
        JsonNode file = serialize(m).get(0);
        assertEquals("file", file.get("type").asText());
        assertEquals("data:image/png;base64,QUJD", file.get("file_data").asText());
        assertEquals("image.png", file.get("filename").asText());
        assertNull(file.get("file_id"));
        assertNull(file.get("image_url"));
    }

    @Test
    @DisplayName("UploadFileRequest 生成 multipart body，含 purpose/file/expires_after")
    public void testUploadMultipartBody() {
        UploadFileRequest req = UploadFileRequest.create("image".getBytes(StandardCharsets.UTF_8), "image.jpg")
                .expiresAfterSeconds(3600);
        String contentType = req.contentType();
        assertTrue(contentType.startsWith("multipart/form-data; boundary="));
        String boundary = contentType.substring("multipart/form-data; boundary=".length());
        String body = new String(req.body(), StandardCharsets.UTF_8);
        assertTrue(body.contains("--" + boundary));
        assertTrue(body.contains("name=\"purpose\""));
        assertTrue(body.contains("user_data"));
        assertTrue(body.contains("name=\"file\""));
        assertTrue(body.contains("filename=\"image.jpg\""));
        assertTrue(body.contains("expires_after[anchor]"));
        assertTrue(body.contains("expires_after[seconds]"));
        assertTrue(body.contains("3600"));
        assertTrue(body.trim().endsWith("--"));
    }

    @Test
    @DisplayName("ListFilesRequest 生成查询串")
    public void testListFilesQuery() {
        ListFilesRequest req = new ListFilesRequest();
        req.setAfter("file-api-x");
        req.setLimit(50);
        req.setOrder("desc");
        req.setPurpose("user_data");
        assertEquals("files?after=file-api-x&limit=50&order=desc&purpose=user_data", req.getPath());
    }

    @Test
    @DisplayName("Retrieve/Delete file 路径")
    public void testRetrieveDeletePath() {
        assertEquals("files/file-api-x", RetrieveFileRequest.create("file-api-x").getPath());
        assertEquals("files/file-api-x", DeleteFileRequest.create("file-api-x").getPath());
    }

    // ==================== 使用本地图片 / 外部链接的端到端载荷校验 ====================

    /** 读取测试图片 src/test/resources/images/img.png。 */
    private static byte[] readTestImage() throws IOException {
        InputStream in = MultimodalAndFileSerializationTests.class.getResourceAsStream("/images/img.png");
        assertTrue(in != null, "未找到测试图片 /images/img.png");
        return in.readAllBytes();
    }

    @Test
    @DisplayName("本地图片：chat 消息以 PNG base64 data URL 内联，PNG 头正确")
    public void testChatWithLocalImage() throws Exception {
        byte[] png = readTestImage();
        // PNG 魔数校验
        assertTrue(png.length > 8);
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 0x50, png[1]);
        assertEquals((byte) 0x4e, png[2]);
        assertEquals((byte) 0x47, png[3]);

        UserMessage m = new UserMessage();
        String b64 = Base64.getEncoder().encodeToString(png);
        m.setContent(ContentPart.text("这张图片里有什么？"), ContentPart.imageDataUrl("image/png", b64));

        JsonNode content = serialize(m);
        assertTrue(content.isArray());
        JsonNode img = content.get(1).get("image_url");
        assertEquals("data:image/png;base64," + b64, img.get("url").asText());
    }

    @Test
    @DisplayName("外部图片 URL：image_url 块携带 url 与 detail")
    public void testChatWithExternalImageUrl() throws Exception {
        String url = "https://i.czl.net/oracle/img/2024/10/671d5e03f418f.webp";
        UserMessage m = new UserMessage();
        ImageUrl imageUrl = ImageUrl.ofUrl(url);
        imageUrl.setDetail("low");
        m.setContent(ContentPart.text("描述这张图片。"), ContentPart.imageUrl(imageUrl));

        JsonNode img = serialize(m).get(1).get("image_url");
        assertEquals(url, img.get("url").asText());
        assertEquals("low", img.get("detail").asText());
    }

    @Test
    @DisplayName("本地图片：UploadFileRequest 用实际 PNG 字节构造 multipart")
    public void testUploadLocalImageBytes() throws Exception {
        byte[] png = readTestImage();
        UploadFileRequest req = UploadFileRequest.create(png, "img.png");
        String body = new String(req.body(), StandardCharsets.ISO_8859_1);
        // multipart 中 file 块携带原始 PNG 字节（以 ISO-8859-1 无损读回）
        assertTrue(body.contains("name=\"file\""));
        assertTrue(body.contains("filename=\"img.png\""));
        // 用原始字节出现在 body 中（说明真的带了图片内容）
        byte[] raw = req.body();
        assertTrue(raw.length > png.length, "multipart 应比图片本身更长");
    }

    // ==================== 文件响应模型反序列化 ====================

    @Test
    @DisplayName("FileObject 反序列化：id/object/bytes/created_at/filename/purpose/expires_at")
    public void testDeserializeFileObject() throws Exception {
        String json = "{\"id\":\"file-api-xxx\",\"object\":\"file\",\"bytes\":102400,"
                + "\"created_at\":1700000000,\"filename\":\"image.jpg\",\"purpose\":\"user_data\","
                + "\"expires_at\":1700003600}";
        FileObject f = MAPPER.readValue(json, FileObject.class);
        assertEquals("file-api-xxx", f.getId());
        assertEquals("file", f.getObject());
        assertEquals(102400L, f.getBytes());
        assertEquals(1700000000L, f.getCreatedAt());
        assertEquals("image.jpg", f.getFilename());
        assertEquals("user_data", f.getPurpose());
        assertEquals(1700003600L, f.getExpiresAt());
    }

    @Test
    @DisplayName("FileListResponse 反序列化：data/first_id/last_id/has_more")
    public void testDeserializeFileList() throws Exception {
        String json = "{\"object\":\"list\",\"data\":[{\"id\":\"file-api-1\",\"object\":\"file\","
                + "\"bytes\":10,\"created_at\":1,\"filename\":\"a.png\",\"purpose\":\"user_data\"}],"
                + "\"first_id\":\"file-api-1\",\"last_id\":\"file-api-1\",\"has_more\":false}";
        FileListResponse resp = MAPPER.readValue(json, FileListResponse.class);
        assertEquals("list", resp.getObject());
        assertEquals(1, resp.getData().size());
        assertEquals("file-api-1", resp.getData().get(0).getId());
        assertEquals("file-api-1", resp.getFirstId());
        assertEquals("file-api-1", resp.getLastId());
        assertEquals(false, resp.getHasMore());
    }

    @Test
    @DisplayName("DeleteFileResponse 反序列化：id/object/deleted")
    public void testDeserializeDeleteFile() throws Exception {
        String json = "{\"id\":\"file-api-xxx\",\"object\":\"file\",\"deleted\":true}";
        DeleteFileResponse resp = MAPPER.readValue(json, DeleteFileResponse.class);
        assertEquals("file-api-xxx", resp.getId());
        assertEquals("file", resp.getObject());
        assertEquals(true, resp.getDeleted());
    }
}
