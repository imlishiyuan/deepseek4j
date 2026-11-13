package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import cn.lishiyuan.deepseek.api.message.MessageStreamEvent;
import cn.lishiyuan.deepseek.api.message.MessageStreamRequest;
import cn.lishiyuan.deepseek.api.tool.TtsRequest;
import cn.lishiyuan.deepseek.api.tool.TtsStreamOptions;
import cn.lishiyuan.deepseek.api.tool.TtsResponse;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface Client {

    <T extends BaseResponse> Mono<T> http(BaseRequest<T> request, String method);

    <T extends BaseResponse> Mono<T> get(BaseRequest<T> request);

    <T extends BaseResponse> Mono<T> post(BaseRequest<T> request);

    <T extends BaseResponse> Mono<T> delete(BaseRequest<T> request);

    <T extends BaseStreamResponse> Flux<T> stream(BaseStreamRequest<T> request);

    /**
     * 二进制响应调用（如 TTS 返回的音频）。
     * <p>请求体按 JSON 序列化；响应体以字节返回，不做 JSON 反序列化。
     * 非 200 时仍按错误 JSON 解析并抛出 {@code DeepSeekException}。
     */
    Mono<byte[]> postForBytes(BaseRequest<?> request);

    /**
     * Responses API 流式调用。
     * <p>与 {@link #stream(BaseStreamRequest)} 不同：Responses 使用命名 SSE 事件
     * （event: &lt;type&gt; + data: &lt;json&gt;），终止事件为 response.completed/incomplete/failed，无 data: [DONE]。
     */
    Flux<ResponseStreamEvent> streamResponse(ResponseStreamRequest request);

    /**
     * Anthropic Messages API 流式调用。
     * <p>命名 SSE 事件（event: &lt;type&gt; + data: &lt;json&gt;），以 {@code message_stop} 结束，无 data: [DONE]。
     */
    Flux<MessageStreamEvent> streamMessage(MessageStreamRequest request);

    /**
     * 语音合成（TTS）：返回音频字节与响应 Content-Type。
     * <p>内部先做本地入参校验（text 非空且不超过 10000 个计费字符）。
     */
    Mono<TtsResponse> tts(TtsRequest request);

    /**
     * TTS WebSocket 流式合成：文本片段边送边合成，返回音频帧流。
     * <p>音频格式由 {@link TtsStreamOptions#getFormat()} 决定（默认 {@code pcm}：24kHz/16bit/mono 原始帧，
     * 也支持 {@code opus} 数据包）。服务端以 {@code {"event":"finish","code":"success"}} 结束时 Flux 正常完成，
     * 其它 code 以错误结束。
     */
    Flux<byte[]> ttsStream(Flux<String> textChunks, TtsStreamOptions options);
}
