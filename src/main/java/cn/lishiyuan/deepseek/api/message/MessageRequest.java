package cn.lishiyuan.deepseek.api.message;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.chat.Thinking;
import cn.lishiyuan.deepseek.api.common.SafetyCheck;
import cn.lishiyuan.deepseek.api.common.TitleOptions;
import cn.lishiyuan.deepseek.config.enums.ModelEnums;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Messages API 请求（{@code POST /anthropic/v1/messages}，非流式）。
 *
 * <p>DeepSeek 的 Anthropic 兼容端点 base_url 为 {@code https://api.deepseek.com/anthropic}，
 * 因此本类的 path 为 {@code anthropic/v1/messages}；鉴权可用 {@code x-api-key} 或
 * {@code Authorization: Bearer}（本 SDK 用后者）。
 *
 * <p>联网工具在 {@code tools} 里声明为 {@code {"type":"deepseek_web_search","name":"deepseek_web_search"}}
 * 与 {@code {"type":"deepseek_read_url","name":"deepseek_read_url"}}（用 {@link #addWebSearchTool()} /
 * {@link #addReadUrlTool()}），搜索与抓取都由服务端完成。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/create-message">Messages API</a>
 */
@Data
public class MessageRequest extends BaseRequest<MessageResponse> {

    /** 模型：deepseek-flash / deepseek-v4-pro（也接受 claude* 别名） */
    @JsonProperty("model")
    private String model = ModelEnums.DEEPSEEK_FLASH.code;

    /** 对话历史，至少 1 条；该 API 无状态，每次都要传完整历史 */
    @JsonProperty("messages")
    private List<MessageInput> messages;

    /** 系统提示词（顶层字段，不是消息） */
    @JsonProperty("system")
    private String system;

    /** 最大输出 token；HTTP 请求可省略，默认值取决于模型与思考模式 */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("stream")
    private final boolean stream = false;

    /** 思考模式：{@code {"type":"enabled"|"disabled"}}（budget_tokens 被忽略） */
    private Thinking thinking;

    /**
     * 工具列表：客户端工具 {@code {name,description,input_schema}}，
     * 或服务端工具 {@code {"type":"deepseek_web_search"|"deepseek_read_url","name":...}}。
     */
    @JsonProperty("tools")
    private List<Object> tools;

    /** 模型内容安全开关（DeepSeek 扩展字段）。 */
    @JsonProperty("safety_check")
    private SafetyCheck safetyCheck;

    /** 会话标题开关（DeepSeek 扩展字段）。流式时标题在 message_delta 的 delta.title。 */
    @JsonProperty("title")
    private TitleOptions title;

    /** 采样温度，[0.0, 2.0]；思考模式下固定为 1.0 */
    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    /** 停止序列（最多 16 个） */
    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    /** 元数据：仅 user_id 生效 */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public static MessageRequest create(List<MessageInput> messages, String model) {
        MessageRequest request = new MessageRequest();
        request.setMessages(messages);
        request.setModel(model);
        return request;
    }

    public static MessageRequest create(String userText, String model) {
        return create(List.of(MessageInput.user(userText)), model);
    }

    /** 声明服务端联网搜索工具（deepseek_web_search）。 */
    public MessageRequest addWebSearchTool() {
        return addServerTool("deepseek_web_search");
    }

    /** 声明服务端阅读链接工具（deepseek_read_url）。 */
    public MessageRequest addReadUrlTool() {
        return addServerTool("deepseek_read_url");
    }

    private MessageRequest addServerTool(String name) {
        if (tools == null) {
            tools = new ArrayList<>();
        }
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", name);
        tool.put("name", name);
        tools.add(tool);
        return this;
    }

    @Override
    public Class<MessageResponse> getResponseClass() {
        return MessageResponse.class;
    }

    @Override
    public String getPath() {
        return "anthropic/v1/messages";
    }
}
