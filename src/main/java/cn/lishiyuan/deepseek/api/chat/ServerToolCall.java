package cn.lishiyuan.deepseek.api.chat;

import cn.lishiyuan.deepseek.api.common.ServerToolAction;
import cn.lishiyuan.deepseek.api.common.ServerToolPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 一次服务端工具调用（{@link ServerToolStep#getCalls()} 的元素）。
 *
 * <p>{@code type} 是开启这项能力的工具名（{@code deepseek_web_search} / {@code deepseek_read_url}），
 * {@code kind} 标明调用属于哪种能力：{@code search}（联网搜索）、{@code deep_search}（深度思考搜索）、
 * {@code read_link}（阅读链接）、{@code image_search} / {@code deep_image_search}（图片搜索）。
 *
 * <p><b>多轮对话</b>：{@code server_tool_calls} 需要随 assistant 消息原样回传，
 * 阅读链接的网页正文加密在 {@code signature} 里，丢弃它将导致之前读过的网页不再进入上下文。
 */
@Data
public class ServerToolCall {

    /** 调用在这一步内的序号。 */
    @JsonProperty("index")
    private Integer index;

    /** 调用 ID（形如 {@code srvtoolu_rl_00_xxx}）。 */
    @JsonProperty("id")
    private String id;

    /** 开启该能力的工具名：{@code deepseek_web_search} 或 {@code deepseek_read_url}。 */
    @JsonProperty("type")
    private String type;

    /** 能力类型：search / deep_search / read_link / image_search / deep_image_search。 */
    @JsonProperty("kind")
    private String kind;

    /** 调用参数。 */
    @JsonProperty("action")
    private ServerToolAction action;

    /** completed / failed；响应结束时还没有结果的调用为 failed。 */
    @JsonProperty("status")
    private String status;

    /** 搜索结果（{@code kind} 为搜索类时返回）。 */
    @JsonProperty("sources")
    private List<ServerToolSource> sources;

    /** 打开/阅读的网页信息，仅供展示。 */
    @JsonProperty("page")
    private ServerToolPage page;

    /** 加密后的网页正文，只有成功阅读的链接才有；回传时必须原样保留。 */
    @JsonProperty("signature")
    private String signature;

    /** 调用失败时的错误信息。 */
    @JsonProperty("error")
    private ServerToolError error;
}
