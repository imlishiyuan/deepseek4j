package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.config.enums.TtsStreamFormatEnums;
import cn.lishiyuan.deepseek.config.enums.TtsVoiceEnums;
import lombok.Data;

/**
 * TTS WebSocket 流式合成的选项（作为查询参数传给 {@code wss://api.deepseek.com/v1/tools/tts}）。
 *
 * <p>与 HTTP 版不同：WebSocket 的音频是<b>分帧返回</b>的，格式只支持 {@code pcm}（默认）和 {@code opus}，
 * 没有 {@code wav}（没有完整文件头）。
 *
 * @see cn.lishiyuan.deepseek.Client#ttsStream(reactor.core.publisher.Flux, TtsStreamOptions)
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/tts">语音合成</a>
 */
@Data
public class TtsStreamOptions {

    /** 音色：stella（女声，默认）/ tide（男声）。 */
    private String voiceId = TtsVoiceEnums.STELLA.code;

    /** 音频格式：pcm（默认）/ opus。 */
    private String format = TtsStreamFormatEnums.PCM.code;

    /** 朗读前是否去除文本中的 Markdown 格式，默认 true。 */
    private Boolean filterMarkdown = true;

    public static TtsStreamOptions create() {
        return new TtsStreamOptions();
    }

    /** 默认选项：stella 女声 + PCM。 */
    public static TtsStreamOptions pcm() {
        return new TtsStreamOptions();
    }

    /** Opus 数据包格式。 */
    public static TtsStreamOptions opus() {
        TtsStreamOptions options = new TtsStreamOptions();
        options.setFormat(TtsStreamFormatEnums.OPUS.code);
        return options;
    }

    public TtsStreamOptions voice(TtsVoiceEnums voice) {
        this.voiceId = voice.code;
        return this;
    }

    public TtsStreamOptions format(TtsStreamFormatEnums format) {
        this.format = format.code;
        return this;
    }

    public TtsStreamOptions filterMarkdown(boolean enabled) {
        this.filterMarkdown = enabled;
        return this;
    }
}
