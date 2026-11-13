package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 语音合成音色（{@code voice_id}）。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/tts">语音合成</a>
 */
@AllArgsConstructor
public enum TtsVoiceEnums {
    /** 女声（默认） */
    STELLA("stella", "女声"),
    /** 男声 */
    TIDE("tide", "男声"),
    ;
    public final String code;
    public final String desc;

    public static TtsVoiceEnums fromCode(String code) {
        for (TtsVoiceEnums anEnum : TtsVoiceEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

}
