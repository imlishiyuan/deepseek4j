package cn.lishiyuan.deepseek.config.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

/**
 * 模型输入 / 输出模态。
 *
 * <p>{@code input_modalities} 可能为 {@code text} / {@code image}；{@code output_modalities} 目前只有 {@code text}。
 *
 * @see cn.lishiyuan.deepseek.api.platform.ListModelResponse.Model
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/list-models">获取模型列表</a>
 */
@AllArgsConstructor
public enum ModalityEnums {

    TEXT("text", "文本"),

    IMAGE("image", "图像"),

    ;
    @JsonValue
    public final String code;
    public final String desc;

    /** 按 code 反序列化；未知值返回 null。 */
    @JsonCreator
    public static ModalityEnums fromCode(String code) {
        for (ModalityEnums anEnum : ModalityEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code;
    }
}
