package cn.lishiyuan.deepseek.api.message;

import cn.lishiyuan.deepseek.api.BaseStreamRequest;
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
 * Anthropic Messages API 流式请求（{@code POST /anthropic/v1/messages}）。
 *
 * <p>流式协议为命名 SSE 事件：{@code event: <type>} + {@code data: <json>}，
 * 事件序列为 message_start → (content_block_start → content_block_delta… → content_block_stop)* →
 * message_delta → message_stop，无 {@code data: [DONE]}。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/create-message">Messages API</a>
 */
@Data
public class MessageStreamRequest extends BaseStreamRequest<MessageStreamEvent> {

    @JsonProperty("model")
    private String model = ModelEnums.DEEPSEEK_FLASH.code;

    @JsonProperty("messages")
    private List<MessageInput> messages;

    @JsonProperty("system")
    private String system;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("stream")
    private final boolean stream = true;

    private Thinking thinking;

    @JsonProperty("tools")
    private List<MessageTool> tools;

    @JsonProperty("safety_check")
    private SafetyCheck safetyCheck;

    @JsonProperty("title")
    private TitleOptions title;

    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    /** 元数据：键值均为任意 JSON；服务端目前仅 {@code user_id}（字符串）生效，其它字段忽略 */
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    public static MessageStreamRequest create(List<MessageInput> messages, String model) {
        MessageStreamRequest request = new MessageStreamRequest();
        request.setMessages(messages);
        request.setModel(model);
        return request;
    }

    public static MessageStreamRequest create(String userText, String model) {
        return create(List.of(MessageInput.user(userText)), model);
    }

    /** 声明服务端联网搜索工具（deepseek_web_search）。 */
    public MessageStreamRequest addWebSearchTool() {
        return addServerTool(MessageTool.webSearch());
    }

    /** 声明服务端阅读链接工具（deepseek_read_url）。 */
    public MessageStreamRequest addReadUrlTool() {
        return addServerTool(MessageTool.readUrl());
    }

    private MessageStreamRequest addServerTool(MessageTool tool) {
        if (tools == null) {
            tools = new ArrayList<>();
        }
        tools.add(tool);
        return this;
    }

    @Override
    public Class<MessageStreamEvent> getResponseClass() {
        return MessageStreamEvent.class;
    }

    @Override
    public String getPath() {
        return "anthropic/v1/messages";
    }
}
