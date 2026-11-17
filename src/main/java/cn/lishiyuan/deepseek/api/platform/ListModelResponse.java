package cn.lishiyuan.deepseek.api.platform;

import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.config.enums.ModalityEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 获取模型列表（{@code GET /models}）的响应。
 *
 * <p>除标识字段外，每个模型还带有机器可读的能力与限制元数据；当某项信息对该模型不可用时，对应字段可能缺省。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/list-models">获取模型列表</a>
 */
@Data
public class ListModelResponse extends BaseResponse {

    /** 对象类型，恒为 {@code list} */
    @JsonProperty("object")
    private String object;

    /** 模型对象列表 */
    @JsonProperty("data")
    private List<Model> data;

    /** 一个可通过 API 使用的 DeepSeek 模型。 */
    @Data
    public static class Model {

        /** 模型的标识符，用于在 API 接口中引用该模型（如 {@code deepseek-flash}） */
        @JsonProperty("id")
        private String id;

        /** 对象类型，恒为 {@code model} */
        @JsonProperty("object")
        private String object;

        /** 拥有该模型的组织 */
        @JsonProperty("owned_by")
        private String ownedBy;

        /** 展示名称，供模型选择器和模型发现工具使用（如 {@code DeepSeek-V4.1-Flash}） */
        @JsonProperty("name")
        private String name;

        /** 上下文窗口的 token 总容量，输入与输出 token 合计 */
        @JsonProperty("context_window")
        private Integer contextWindow;

        /** 服务端允许单次响应输出的 token 数上限，即 {@code max_tokens} 可接受的最大值 */
        @JsonProperty("max_output_tokens")
        private Integer maxOutputTokens;

        /** 模型接受的输入类型，取值见 {@link ModalityEnums}：{@code text} / {@code image} */
        @JsonProperty("input_modalities")
        private List<String> inputModalities;

        /** 模型可生成的媒体类型，取值见 {@link ModalityEnums}：{@code text} */
        @JsonProperty("output_modalities")
        private List<String> outputModalities;

        /** 开启思考模式时可用的推理强度（effort）档位 */
        @JsonProperty("effort")
        private Effort effort;

        /** 按 API 协议声明的模型行为 */
        @JsonProperty("api_capabilities")
        private ApiCapabilities apiCapabilities;

        /** 是否接受指定的输入模态（如 {@link ModalityEnums#IMAGE}）。 */
        public boolean supportsInput(ModalityEnums modality) {
            return modality != null && inputModalities != null && inputModalities.contains(modality.code);
        }

        /** 是否支持指定的输出模态。 */
        public boolean supportsOutput(ModalityEnums modality) {
            return modality != null && outputModalities != null && outputModalities.contains(modality.code);
        }
    }

    /** 推理强度档位。 */
    @Data
    public static class Effort {

        /**
         * 支持的推理强度档位，按建议的展示顺序排列。
         * <p>这些值即 {@code reasoning_effort} 参数可接受的取值；用于关闭思考模式的 {@code none} 不包含在内。
         */
        @JsonProperty("supported_levels")
        private List<String> supportedLevels;

        /** 开启思考模式且请求未指定推理强度时，服务端使用的默认档位（一定属于 {@link #supportedLevels}） */
        @JsonProperty("default_level")
        private String defaultLevel;
    }

    /** 按 API 协议声明的模型行为。 */
    @Data
    public static class ApiCapabilities {

        /** 通过 Anthropic Messages API 使用该模型时适用的能力 */
        @JsonProperty("anthropic_messages")
        private AnthropicMessages anthropicMessages;
    }

    /** 通过 Anthropic Messages API 使用该模型时适用的能力。 */
    @Data
    public static class AnthropicMessages {

        /**
         * 对话过程中更新 system 提示词时，模型采用新提示词的方式：
         * <ul>
         *   <li>{@code leading-only}：只有对话开头的 system 提示词生效；</li>
         *   <li>{@code in-history}：可在对话历史中追加 system 消息，最新一条提供完整的有效提示词。</li>
         * </ul>
         */
        @JsonProperty("system_prompt_update")
        private String systemPromptUpdate;

        /**
         * 对话过程中更新工具时，模型采用新工具的方式：
         * <ul>
         *   <li>{@code addition-only}：只允许在历史中追加工具；</li>
         *   <li>{@code leading-only}：只有对话开头的 tools 生效。</li>
         * </ul>
         * <p>注：官方 schema 暂未列出该字段，但线上会返回。
         */
        @JsonProperty("tool_update")
        private String toolUpdate;
    }
}
