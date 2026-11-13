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
import cn.lishiyuan.deepseek.api.tool.AsrRequest;
import cn.lishiyuan.deepseek.api.tool.AsrResponse;
import cn.lishiyuan.deepseek.api.tool.TtsRequest;
import cn.lishiyuan.deepseek.api.tool.TtsResponse;
import cn.lishiyuan.deepseek.api.tool.TtsStreamOptions;
import cn.lishiyuan.deepseek.api.tool.OcrRequest;
import cn.lishiyuan.deepseek.api.tool.OcrResponse;
import cn.lishiyuan.deepseek.config.enums.FilePurposeEnums;
import cn.lishiyuan.deepseek.config.enums.TtsFormatEnums;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 视觉（多模态）端到端集成测试：仅当接入 accessKey 可用时执行。
 * 会真实调用 DeepSeek API 并消耗额度，请按需运行。
 * <p>注：不在 {@code @BeforeAll} 中初始化，以免部分旧版 surefire 不执行静态生命周期方法。
 */
@DisplayName("多模态端到端集成测试")
public class VisionIntegrationTests {

    /** 临时视觉模型，模型名以字符串直接传入（未加入 ModelEnums）。 */
    private static final String VISION_MODEL = "deepseek-flash";

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

    /** 读取测试资源（图片 / 文档 / 音频）。 */
    private static byte[] readResourceBytes(String resource) throws IOException {
        try (InputStream in = VisionIntegrationTests.class.getResourceAsStream(resource)) {
            Assertions.assertNotNull(in, "未找到测试资源：" + resource);
            return in.readAllBytes();
        }
    }

    private static byte[] readImageBytes(String resource) throws IOException {
        return readResourceBytes(resource);
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

    @Test
    @DisplayName("以 purpose=text_recognition 上传 PDF，并在对话中以 file_id 引用")
    public void testUploadDocumentWithPurposeAndChat() throws IOException {
        assumeClient();
        byte[] pdf = readResourceBytes("/file/AI模型与内容资产市场调研.pdf");

        // 1) 带 purpose 上传：文档在上传时完成解析，解析成功后才返回 file_id
        UploadFileRequest upload = UploadFileRequest.create(pdf, "AI模型与内容资产市场调研.pdf")
                .purpose(FilePurposeEnums.TEXT_RECOGNITION);
        FileObject file = CLIENT.post(upload).block();
        Assertions.assertNotNull(file, "上传文档结果不应为空");
        Assertions.assertTrue(file.getId().startsWith("file-api-"),
                "file_id 应以 file-api- 开头：" + file.getId());
        Assertions.assertEquals(FilePurposeEnums.TEXT_RECOGNITION.code, file.getPurpose(),
                "返回的 purpose 应回显 text_recognition");
        System.out.println("文档 file_id：" + file.getId() + ", bytes=" + file.getBytes());

        try {
            // 2) 在对话中以 file 内容块引用该文档（Chat Completions 用 file_id 引用）
            UserMessage msg = new UserMessage();
            msg.setContent(ContentPart.file(file.getId()), ContentPart.text("用一句话说明这份文档的主题。"));
            ChatRequest req = ChatRequest.create(List.of(msg), VISION_MODEL);
            ChatResponse resp = CLIENT.post(req).block();

            Assertions.assertNotNull(resp, "文档对话响应不应为空");
            String content = resp.getChoices().get(0).getMessage().getContent();
            Assertions.assertNotNull(content, "回答不应为空");
            Assertions.assertFalse(content.isBlank(), "回答不应为空白");
            // 文档主题是「AI 模型能力与 AI 内容资产市场」，回答应能体现出看了文档
            Assertions.assertTrue(content.contains("AI") || content.contains("人工智能"),
                    "回答应体现文档内容，实际：" + content);
            System.out.println("文档对话回答：" + content);
        } finally {
            // 清理上传的文件
            CLIENT.delete(DeleteFileRequest.create(file.getId())).block();
        }
    }

    @Test
    @DisplayName("OCR 识别图片中的文字")
    public void testOcr() throws IOException {
        assumeClient();
        byte[] png = readImageBytes("/images/img.png");

        OcrResponse resp = CLIENT.post(OcrRequest.create(png, "img.png")).block();
        Assertions.assertNotNull(resp, "OCR 响应不应为空");
        Assertions.assertTrue(resp.getId().startsWith("ocr-"), "OCR id 应以 ocr- 开头：" + resp.getId());
        Assertions.assertEquals("ocr.result", resp.getObject());
        Assertions.assertEquals(FilePurposeEnums.TEXT_RECOGNITION.code, resp.getPurpose(),
                "默认按 text_recognition 识别");

        Assertions.assertNotNull(resp.getPages(), "pages 不应为空");
        Assertions.assertFalse(resp.getPages().isEmpty(), "pages 不应为空");
        Assertions.assertEquals(resp.getPages().size(), resp.getPageCount(),
                "page_count 应等于 pages 的数量");

        String text = resp.getPages().get(0).getText();
        Assertions.assertNotNull(text, "识别文本不应为空");
        Assertions.assertFalse(text.isBlank(), "识别文本不应为空白");
        // 这张图是 NEXUS 官网截图，识别结果应包含画面中的 NEXUS
        Assertions.assertTrue(text.contains("NEXUS"), "识别文本应包含 NEXUS，实际：" + text);
        System.out.println("OCR 识别长度：" + text.length() + "，page_count=" + resp.getPageCount());
    }

    @Test
    @DisplayName("ASR 识别 Ogg Opus 音频为文本")
    public void testAsr() throws IOException {
        assumeClient();
        byte[] audio = readResourceBytes("/file/诗歌.wav");

        AsrResponse resp = CLIENT.post(AsrRequest.create(audio, "诗歌.wav").oggOpus()).block();
        Assertions.assertNotNull(resp, "ASR 响应不应为空");
        Assertions.assertNotNull(resp.getRequestId(), "应返回 request_id");
        Assertions.assertNotNull(resp.getText(), "识别文本不应为空");
        Assertions.assertFalse(resp.getText().isBlank(), "识别文本不应为空白");
        Assertions.assertNotNull(resp.getDurationMs(), "应返回音频时长");
        Assertions.assertTrue(resp.getDurationMs() > 0, "音频时长应大于 0，实际：" + resp.getDurationMs());
        // 音频内容是《沁园春·雪》
        Assertions.assertTrue(resp.getText().contains("沁园春") || resp.getText().contains("雪"),
                "识别文本应体现音频内容，实际：" + resp.getText());
        System.out.println("ASR 识别（" + resp.getDurationMs() + "ms）：" + resp.getText());
    }

    // ───────────────────────── TTS 语音合成 ─────────────────────────

    @Test
    @DisplayName("TTS：合成为 WAV 并保存到下载目录")
    public void testTtsWav() {
        assumeClient();
        TtsRequest request = TtsRequest.create("你好，我是 DeepSeek。");

        TtsResponse response = CLIENT.tts(request).block();
        Assertions.assertNotNull(response, "TTS 响应不应为空");
        Assertions.assertTrue(response.length() > 0, "音频不应为空");
        Assertions.assertTrue(response.isWav(), "应返回 WAV，实际 contentType：" + response.getContentType());
        Assertions.assertTrue(response.getContentType().startsWith("audio/wav"),
                "Content-Type 应为 audio/wav，实际：" + response.getContentType());

        byte[] audio = response.getAudio();
        Assertions.assertTrue(audio.length > 12, "WAV 长度应大于 12 字节");
        Assertions.assertEquals("RIFF", new String(audio, 0, 4, StandardCharsets.US_ASCII));
        Assertions.assertEquals("WAVE", new String(audio, 8, 4, StandardCharsets.US_ASCII));

        Path saved = saveTts(response, "deepseek-tts-test.wav");
        System.out.println("TTS WAV 已保存：" + saved + "（" + response.length() + " 字节）");
    }

    @Test
    @DisplayName("TTS：合成为原始 PCM 并保存到下载目录")
    public void testTtsPcm() {
        assumeClient();
        TtsRequest request = TtsRequest.create("你好，我是 DeepSeek。").format(TtsFormatEnums.PCM);

        TtsResponse response = CLIENT.tts(request).block();
        Assertions.assertNotNull(response, "TTS 响应不应为空");
        Assertions.assertTrue(response.length() > 0, "音频不应为空");
        Assertions.assertTrue(response.isPcm(), "应返回 PCM，实际 contentType：" + response.getContentType());
        Assertions.assertTrue(response.getContentType().startsWith("audio/pcm"),
                "Content-Type 应为 audio/pcm，实际：" + response.getContentType());
        // 24kHz / 16 位 / 单声道：每采样 2 字节，总字节数应为偶数
        Assertions.assertEquals(0, response.getAudio().length % 2, "16 位 PCM 字节数应为偶数");

        Path saved = saveTts(response, "deepseek-tts-test.pcm");
        System.out.println("TTS PCM 已保存：" + saved + "（" + response.length() + " 字节）");
    }

    @Test
    @DisplayName("TTS：本地校验（空文本/超长文本直接失败，不发请求）")
    public void testTtsValidation() {
        assumeClient();
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> CLIENT.tts(TtsRequest.create("   ")).block());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> CLIENT.tts(TtsRequest.create("a".repeat(TtsRequest.MAX_TEXT_LENGTH + 1))).block());
    }

    @Test
    @DisplayName("TTS WebSocket 流式：每秒送一句，收到的音频帧实时写入文件")
    public void testTtsStream() throws IOException {
        assumeClient();

        // 每秒投放一句话，模拟模型流式输出
        List<String> sentences = List.of("你好，", "我是 ", "DeepSeek 语音合成。", "很高兴为你服务。");
        Flux<String> textStream = Flux.fromIterable(sentences).delayElements(Duration.ofSeconds(1));

        Files.createDirectories(TTS_OUTPUT_DIR);
        Path pcmFile = TTS_OUTPUT_DIR.resolve("deepseek-tts-stream.pcm");

        // 接收端：打开文件，订阅音频帧并持续写入（不在内存里累计字节）
        AtomicInteger frameCount = new AtomicInteger();
        try (OutputStream out = Files.newOutputStream(pcmFile)) {
            CLIENT.ttsStream(textStream, TtsStreamOptions.pcm())
                    .doOnNext(frame -> {
                        try {
                            out.write(frame);
                            out.flush();
                        } catch (IOException e) {
                            throw new UncheckedIOException("写入音频帧失败", e);
                        }
                        frameCount.incrementAndGet();
                    })
                    .blockLast(Duration.ofSeconds(120));
        }

        long size = Files.size(pcmFile);
        Assertions.assertTrue(size > 0, "写入的 PCM 文件应有内容");
        Assertions.assertEquals(0, size % 2, "16 位 PCM 字节数应为偶数");
        System.out.println("TTS 流式：帧数=" + frameCount.get() + "，文件=" + pcmFile
                + "，大小=" + size + " 字节（约 " + String.format("%.2f", size / 2.0 / 24000) + " 秒）");
    }

    /** TTS 音频保存目录：用户主目录下的「下载」。 */
    private static final Path TTS_OUTPUT_DIR = Paths.get(System.getProperty("user.home"), "下载");

    /** 把 TTS 音频保存到下载目录，返回文件路径。 */
    private static Path saveTts(TtsResponse response, String filename) {
        try {
            Files.createDirectories(TTS_OUTPUT_DIR);
            Path target = TTS_OUTPUT_DIR.resolve(filename);
            int bytes = response.writeTo(target);
            Assertions.assertTrue(Files.exists(target), "音频文件应存在：" + target);
            Assertions.assertEquals(response.length(), Files.size(target), "落盘大小应与音频字节数一致");
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException("保存 TTS 音频失败：" + filename, e);
        }
    }

}
