package cn.lishiyuan.deepseek.api.response;

import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Responses API 流式请求。
 * <p>流式协议与 chat/FIM 不同：使用命名 SSE 事件（event: &lt;type&gt; + data: &lt;json&gt;），
 * 终止事件为 response.completed/incomplete/failed，无 data: [DONE]。
 * https://api-docs.deepseek.com/zh-cn/api/create-response
 */
@Data
public class ResponseStreamRequest extends BaseStreamRequest<ResponseStreamEvent> {

    /** 仅支持 deepseek-v4-flash */
    @JsonProperty("model")
    private String model = ModelEnums.DEEPSEEK_V4_FLASH.code;

    /**
     * 输入：可为字符串（视为单条 user 消息）或输入项数组 {@code List<InputItem>}。
     * input 与 instructions 至少传一个。
     */
    @JsonProperty("input")
    private Object input;

    /** 系统级指令，置于上下文最前的 system 消息 */
    @JsonProperty("instructions")
    private String instructions;

    /** 推理配置 */
    @JsonProperty("reasoning")
    private Reasoning reasoning;

    /** 最大输出 token（含可见输出与思考链 token） */
    @JsonProperty("max_output_tokens")
    private Integer maxOutputTokens;

    @JsonProperty("stream")
    private final boolean stream = true;

    /** ≤2，默认 1；思考模式开启时不生效 */
    @JsonProperty("temperature")
    private Double temperature;

    /** ≤1，默认 1；思考模式开启时不生效 */
    @JsonProperty("top_p")
    private Double topP;

    /** 文本输出配置 */
    @JsonProperty("text")
    private Text text;

    /** 工具列表：function 或内置 web_search */
    @JsonProperty("tools")
    private List<Tool> tools;

    /**
     * 字符串 none/auto/required（见 {@link cn.lishiyuan.deepseek.config.enums.ToolChoiceEnums}），
     * 或对象 {"type":"function","name":"..."} 强制调用指定函数，
     * 或 {"type":"web_search"} 强制联网搜索（需 tools 含 web_search）。
     */
    @JsonProperty("tool_choice")
    private Object toolChoice;

    /** ≤20，返回每个位置 top N token 的对数概率 */
    @JsonProperty("top_logprobs")
    private Integer topLogprobs;

    /** 自定义 user 标识，字符集 [a-zA-Z0-9\-_]，最大 512 */
    @JsonProperty("user")
    private String user;

    @Override
    public Class<ResponseStreamEvent> getResponseClass() {
        return ResponseStreamEvent.class;
    }

    @Override
    public String getPath() {
        return "responses";
    }

    public static ResponseStreamRequest create(String input, String model) {
        ResponseStreamRequest req = new ResponseStreamRequest();
        req.setInput(input);
        req.setModel(model);
        return req;
    }

    public static ResponseStreamRequest create(List<InputItem> input, String model) {
        ResponseStreamRequest req = new ResponseStreamRequest();
        req.setInput(input);
        req.setModel(model);
        return req;
    }
}
