package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface Client {

    <T extends BaseResponse> Mono<T> http(BaseRequest<T> request, String method);

    <T extends BaseResponse> Mono<T> get(BaseRequest<T> request);

    <T extends BaseResponse> Mono<T> post(BaseRequest<T> request);

    <T extends BaseStreamResponse> Flux<T> stream(BaseStreamRequest<T> request);

    /**
     * Responses API 流式调用。
     * <p>与 {@link #stream(BaseStreamRequest)} 不同：Responses 使用命名 SSE 事件
     * （event: &lt;type&gt; + data: &lt;json&gt;），终止事件为 response.completed/incomplete/failed，无 data: [DONE]。
     */
    Flux<ResponseStreamEvent> streamResponse(ResponseStreamRequest request);
}
