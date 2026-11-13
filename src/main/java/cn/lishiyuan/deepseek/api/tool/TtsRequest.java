package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.config.enums.TtsFormatEnums;
import cn.lishiyuan.deepseek.config.enums.TtsVoiceEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 语音合成请求（{@code POST /v1/tools/tts}，JSON）。
 *
 * <p>把文本合成为语音，音频边合成边在响应体中返回。
 * <b>响应是二进制音频，不是 JSON</b>，请用 {@code Client.tts(TtsRequest)} 获取：
 * <pre>{@code
 * TtsRequest request = TtsRequest.create("你好，世界");
 * TtsResponse tts = client.tts(request).block();
 * tts.writeTo(Paths.get("speech.wav"));   // format=wav 时是完整 WAV 文件
 * }</pre>
 *
 * <p>限制：{@code text} 不能为空、最多 {@value #MAX_TEXT_LENGTH} 个计费字符，请求体最大 64 KiB；
 * 单次请求最多返回 30 分钟的音频。语种根据文本自动识别，不支持调节语速。
 *
 * <p>如需分段发送文本（例如模型还在生成时），官方还提供 WebSocket 版本
 * （{@code wss://api.deepseek.com/v1/tools/tts}），本 SDK 暂未实现。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/tts">语音合成 API</a>
 */
@Data
public class TtsRequest extends BaseRequest<TtsResponse> {

    /** 文本上限（计费字符） */
    public static final int MAX_TEXT_LENGTH = 10000;

    /** 要朗读的文本，不能为空。 */
    @JsonProperty("text")
    private String text;

    /** 音色：stella（女声，默认）/ tide（男声）。 */
    @JsonProperty("voice_id")
    private String voiceId = TtsVoiceEnums.STELLA.code;

    /** 音频格式：wav（默认，audio/wav）/ pcm（24kHz、16 位有符号、单声道原始 PCM，audio/pcm）。 */
    @JsonProperty("format")
    private String format = TtsFormatEnums.WAV.code;

    /** 朗读前是否去除文本中的 Markdown 格式，默认 true。 */
    @JsonProperty("filter_markdown")
    private Boolean filterMarkdown = true;

    public static TtsRequest create(String text) {
        TtsRequest request = new TtsRequest();
        request.setText(text);
        return request;
    }

    /** 设置音色。 */
    public TtsRequest voice(TtsVoiceEnums voice) {
        this.voiceId = voice.code;
        return this;
    }

    /** 设置音频格式。 */
    public TtsRequest format(TtsFormatEnums format) {
        this.format = format.code;
        return this;
    }

    /** 是否去除 Markdown 格式。 */
    public TtsRequest filterMarkdown(boolean enabled) {
        this.filterMarkdown = enabled;
        return this;
    }

    /**
     * 本地入参校验：text 非空、长度不超过 {@value #MAX_TEXT_LENGTH}。
     * <p>在发起请求前快速失败，避免无意义的网络往返。
     */
    public void validate() {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("TTS 的 text 不能为空");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("TTS 的 text 不能超过 " + MAX_TEXT_LENGTH
                    + " 个计费字符，实际 " + text.length());
        }
    }

    @Override
    public Class<TtsResponse> getResponseClass() {
        return TtsResponse.class;
    }

    @Override
    public String getPath() {
        return "v1/tools/tts";
    }
}
