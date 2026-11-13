package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * TTS WebSocket 流式合成的音频格式（查询参数 {@code format}）。
 *
 * <p>注意与 HTTP 版（{@link TtsFormatEnums}）不同：WebSocket 只支持 {@code pcm} 与 {@code opus}，
 * 不支持 {@code wav}（因为音频是分帧返回的，没有完整文件头）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/tts">语音合成</a>
 */
@AllArgsConstructor
public enum TtsStreamFormatEnums {
    /** 24 kHz、16 位有符号、单声道的原始 PCM 帧（默认） */
    PCM("pcm", "24kHz/16bit/mono 原始 PCM 帧"),
    /** 不带容器的 Opus 数据包（不能直接保存为 Ogg 文件） */
    OPUS("opus", "不带容器的 Opus 数据包"),
    ;
    public final String code;
    public final String desc;

    public static TtsStreamFormatEnums fromCode(String code) {
        for (TtsStreamFormatEnums anEnum : TtsStreamFormatEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

}
