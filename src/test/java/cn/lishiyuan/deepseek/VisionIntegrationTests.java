package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.chat.ChatRequest;
import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.api.chat.msg.content.ContentPart;
import cn.lishiyuan.deepseek.api.file.DeleteFileRequest;
import cn.lishiyuan.deepseek.api.file.DeleteFileResponse;
import cn.lishiyuan.deepseek.api.file.FileListResponse;
import cn.lishiyuan.deepseek.api.file.FileObject;
import cn.lishiyuan.deepseek.api.file.ListFilesRequest;
import cn.lishiyuan.deepseek.api.file.RetrieveFileRequest;
import cn.lishiyuan.deepseek.api.file.UploadFileRequest;
import cn.lishiyuan.deepseek.api.response.ContentBlock;
import cn.lishiyuan.deepseek.api.response.InputItem;
import cn.lishiyuan.deepseek.api.response.ResponseRequest;
import cn.lishiyuan.deepseek.api.response.ResponseResult;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 视觉（多模态）端到端集成测试：仅当接入 accessKey 可用时执行。
 * 会真实调用 DeepSeek API 并消耗额度，请按需运行。
 * <p>注：不在 {@code @BeforeAll} 中初始化，以免部分旧版 surefire 不执行静态生命周期方法。
 */
@DisplayName("多模态端到端集成测试")
public class VisionIntegrationTests {

    /** 临时视觉模型，模型名以字符串直接传入（未加入 ModelEnums）。 */
    private static final String VISION_MODEL = "deepseek-v4-flash-vision-exp";

    private static final Client CLIENT = initClient();

    private static Client initClient() {
        try (InputStream keyIn = VisionIntegrationTests.class.getResourceAsStream("/accessKey.txt")) {
            if (keyIn == null) {
                return null; // 无 accessKey，测试将通过 assumeTrue 跳过
            }
            return new DefaultClient(new String(keyIn.readAllBytes(), StandardCharsets.UTF_8).trim());
        } catch (IOException e) {
            throw new UncheckedIOException("读取 accessKey 失败", e);
        }
    }

    private static byte[] readImageBytes(String resource) throws IOException {
        try (InputStream in = VisionIntegrationTests.class.getResourceAsStream(resource)) {
            Assertions.assertNotNull(in, "未找到图片：" + resource);
            return in.readAllBytes();
        }
    }

    private static void assumeClient() {
        Assumptions.assumeTrue(CLIENT != null, "无 accessKey.txt，跳过集成测试");
    }

    @Test
    @DisplayName("上传本地图片到 Files API，并以 file_id 在多模态 chat 中引用")
    public void testUploadAndChatWithFileId() throws IOException {
        assumeClient();
        byte[] png = readImageBytes("/images/img.png");
        UploadFileRequest upload = UploadFileRequest.create(png, "img.png");
        FileObject file = CLIENT.post(upload).block();
        Assertions.assertNotNull(file, "上传文件结果不应为空");
        Assertions.assertTrue(file.getId().startsWith("file-api-"),
                "file_id 应以 file-api- 开头：" + file.getId());

        UserMessage msg = new UserMessage();
        msg.setContent(ContentPart.text("这张图片里有什么？"), ContentPart.file(file.getId()));
        ChatRequest req = ChatRequest.create(List.of(msg), VISION_MODEL);
        ChatResponse resp = CLIENT.post(req).block();
        Assertions.assertNotNull(resp, "多模态 chat 响应不应为空");
        Assertions.assertTrue(resp.getChoices().get(0).getMessage().getContent().length() > 0,
                "应返回图片描述文本");
    }

    @Test
    @DisplayName("chat 以本地图片 base64 内联传入")
    public void testChatWithLocalImageBase64() throws IOException {
        assumeClient();
        String b64 = Base64.getEncoder().encodeToString(readImageBytes("/images/img.png"));
        UserMessage msg = new UserMessage();
        msg.setContent(ContentPart.text("描述这张图片。"), ContentPart.imageDataUrl("image/png", b64));
        ChatRequest req = ChatRequest.create(List.of(msg), VISION_MODEL);
        ChatResponse resp = CLIENT.post(req).block();
        Assertions.assertNotNull(resp, "多模态 chat 响应不应为空");
        Assertions.assertTrue(resp.getChoices().get(0).getMessage().getContent().length() > 0);
    }

    @Test
    @DisplayName("chat 以外部图片 URL 传入")
    public void testChatWithExternalImageUrl() {
        assumeClient();
        // 用户提供的可用外部图片（WebP）。
        String url = "https://i.czl.net/oracle/img/2024/10/671d5e03f418f.webp";
        UserMessage msg = new UserMessage();
        msg.setContent(ContentPart.text("这张图片里有什么？"), ContentPart.imageUrl(url));
        ChatRequest req = ChatRequest.create(List.of(msg), VISION_MODEL);
        try {
            ChatResponse resp = CLIENT.post(req).block();
            Assertions.assertNotNull(resp, "多模态 chat 响应不应为空");
            Assertions.assertTrue(resp.getChoices().get(0).getMessage().getContent().length() > 0);
        } catch (DeepSeekException e) {
            // 兜底：若服务端无法下载该第三方图片（远程主机可达性问题），
            // 它仍会进入图片下载阶段，说明 image_url 载荷格式正确。
            Assertions.assertTrue(e.getMessage().contains("Failed to download image"),
                    "若失败应为图片下载失败（证明请求体格式正确），实际：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("Responses API 以 input_image 传入本地图片 base64")
    public void testResponsesWithImage() throws IOException {
        assumeClient();
        String b64 = Base64.getEncoder().encodeToString(readImageBytes("/images/img.png"));
        InputItem msg = new InputItem();
        msg.setRole("user");
        msg.setContent(List.of(
                ContentBlock.text("input_text", "描述这张图片。"),
                ContentBlock.inputImage("data:image/png;base64," + b64)));
        ResponseRequest req = ResponseRequest.create(List.of(msg), VISION_MODEL);
        ResponseResult resp = CLIENT.post(req).block();
        Assertions.assertNotNull(resp, "Responses 响应不应为空");
        Assertions.assertNotNull(resp.getOutput());
        Assertions.assertFalse(resp.getOutput().isEmpty(), "输出项不应为空");
    }

    @Test
    @DisplayName("Files API 全生命周期：上传 → 列表 → 查询 → 删除")
    public void testFileLifecycle() throws IOException {
        assumeClient();
        byte[] png = readImageBytes("/images/img.png");

        // 上传
        FileObject uploaded = CLIENT.post(UploadFileRequest.create(png, "img.png")).block();
        Assertions.assertNotNull(uploaded, "上传结果不应为空");
        String fileId = uploaded.getId();
        Assertions.assertTrue(fileId.startsWith("file-api-"), "上传 file_id 应以 file-api- 开头：" + fileId);

        // 列表（应能查到刚上传的文件，或至少成功返回）
        FileListResponse list = CLIENT.get(ListFilesRequest.create()).block();
        Assertions.assertNotNull(list, "列出文件结果不应为空");
        Assertions.assertNotNull(list.getData(), "列表 data 不应为空");

        // 查询单个文件
        FileObject retrieved = CLIENT.get(RetrieveFileRequest.create(fileId)).block();
        Assertions.assertNotNull(retrieved, "查询单个文件结果不应为空");
        Assertions.assertEquals(fileId, retrieved.getId());

        // 删除
        DeleteFileResponse deleted = CLIENT.delete(DeleteFileRequest.create(fileId)).block();
        Assertions.assertNotNull(deleted, "删除结果不应为空");
        Assertions.assertEquals(true, deleted.getDeleted());
    }
}
