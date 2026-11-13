package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 语音合成的音频格式（{@code format}）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/tts">语音合成</a>
 */
@AllArgsConstructor
public enum TtsFormatEnums {
    /** WAV 文件，响应 Content-Type 为 audio/wav（默认） */
    WAV("wav", "audio/wav"),
    /** 24 kHz、16 位有符号、单声道的原始 PCM，响应 Content-Type 为 audio/pcm */
    PCM("pcm", "audio/pcm"),
    ;
    public final String code;
    /** 对应的响应 Content-Type */
    public final String contentType;

    public static TtsFormatEnums fromCode(String code) {
        for (TtsFormatEnums anEnum : TtsFormatEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

}
