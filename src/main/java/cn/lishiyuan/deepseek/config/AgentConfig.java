package cn.lishiyuan.deepseek.config;

import cn.lishiyuan.deepseek.agent.ToolsCaller;
import cn.lishiyuan.deepseek.api.chat.ResponseFormat;
import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.common.ToolChoice;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import cn.lishiyuan.deepseek.config.enums.SendTimeEnums;
import cn.lishiyuan.deepseek.config.enums.ThinkingEffortEnums;
import cn.lishiyuan.deepseek.config.enums.ThinkingEnums;
import lombok.Data;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AgentConfig {
    private final String id;
    private String name;
    private String version;
    private String systemPrompt;
    /**
     * 默认插队时机
     */
    private SendTimeEnums defaultSendTime = SendTimeEnums.NEXT_TURN;

    private ThinkingEnums thinking = ThinkingEnums.ENABLED;

    private ThinkingEffortEnums thinkingEffort = ThinkingEffortEnums.HIGH;

    private ModelEnums model = ModelEnums.DEEPSEEK_FLASH;

    /**
     * 256k = 256 * 1024。这里使用1000
     */
    private Integer maxTokens = 256 * 1000;

    private ResponseFormat responseFormat;

    private List<String> stop;

    private Double temperature = 1.0;

    private Double topP = 1.0;

    private ToolsCaller toolsCaller;

    /**
     * 可取字符串 "none"/"auto"/"required"（见 {@link cn.lishiyuan.deepseek.config.enums.ToolChoiceEnums}），
     * 或对象形式 {"type":"function","function":{"name":"..."}} 强制调用指定 tool。 {@link ToolChoice}
     */
    private Object toolChoice;

    private Boolean logprobs;

    private Integer topLogprobs;

}
