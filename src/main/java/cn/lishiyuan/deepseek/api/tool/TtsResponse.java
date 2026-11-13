package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TTS 语音合成结果：音频字节 + 响应 Content-Type。
 *
 * <p><b>这不是 JSON 响应</b>，请通过 {@code Client.tts(TtsRequest)} 获取：
 * <pre>{@code
 * TtsResponse tts = client.tts(TtsRequest.create("你好，我是 DeepSeek。")).block();
 * tts.writeTo(Paths.get("speech.wav"));   // wav 格式时可直接保存为 .wav 文件
 * }</pre>
 *
 * <p>{@code wav} 返回 {@code audio/wav}（完整 WAV 文件）；{@code pcm} 返回 {@code audio/pcm}
 * （24 kHz、16 位有符号、单声道的原始 PCM，没有文件头，需要自行封装或按参数播放）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/tts">语音合成 API</a>
 */
@Data
public class TtsResponse extends BaseResponse {

    /** 音频字节：WAV 文件或原始 PCM。 */
    @JsonIgnore
    private byte[] audio;

    /** 响应 Content-Type：{@code audio/wav} 或 {@code audio/pcm}（PCM 会带 rate/bits/channels 等参数）。 */
    @JsonIgnore
    private String contentType;

    /** 请求的音频格式：wav / pcm（见 {@link cn.lishiyuan.deepseek.config.enums.TtsFormatEnums}）。 */
    @JsonIgnore
    private String format;

    /** 音频字节数。 */
    public int length() {
        return audio == null ? 0 : audio.length;
    }

    /** 响应是否为 WAV（完整文件，含 RIFF 头）。 */
    public boolean isWav() {
        return (contentType != null && contentType.startsWith("audio/wav"))
                || (contentType == null && "wav".equals(format));
    }

    /** 响应是否为原始 PCM（24 kHz、16 位、单声道）。 */
    public boolean isPcm() {
        return (contentType != null && contentType.startsWith("audio/pcm"))
                || (contentType == null && "pcm".equals(format));
    }

    /**
     * 把音频写入文件，返回写入的字节数。
     */
    public int writeTo(Path path) throws IOException {
        if (audio == null || audio.length == 0) {
            throw new IllegalStateException("音频内容为空，无法写入文件");
        }
        Files.write(path, audio);
        return audio.length;
    }
}
