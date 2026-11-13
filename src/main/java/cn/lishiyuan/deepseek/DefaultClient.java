package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import cn.lishiyuan.deepseek.api.MultipartBodyRequest;
import cn.lishiyuan.deepseek.api.message.MessageStreamEvent;
import cn.lishiyuan.deepseek.api.message.MessageStreamRequest;
import cn.lishiyuan.deepseek.api.tool.TtsRequest;
import cn.lishiyuan.deepseek.api.tool.TtsStreamOptions;
import cn.lishiyuan.deepseek.api.tool.TtsResponse;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.Config;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import cn.lishiyuan.deepseek.e.DeepSeekErrorEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class DefaultClient implements Client {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // 忽略 transient 字段（EmptyRequest 的 responseClass/path）
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            // 响应中可能出现模型未定义的字段，忽略以避免反序列化失败
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            // 不序列化 null 值字段（与原 fastjson2 默认行为一致，请求体更紧凑）
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private final Config config;

    public DefaultClient(String accessKey) {
        this(new Config(accessKey));
    }

    public DefaultClient(String accessKey, String baseUrl) {
        this(new Config(accessKey,baseUrl));
    }

    public DefaultClient(Config config) {
        this.config = config;
        initClient();
    }


    private HttpClient client;


    private void initClient(){
        this.client = HttpClient.newBuilder().connectTimeout(config.getConnectTimeout()).build();
    }

    private HttpRequest.Builder defaultHeader(String accept) {
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + config.getAccessKey())
                .header("Accept", accept);
    }

    private HttpRequest buildHttpRequest(Object body, String path, String method) {
        return buildHttpRequest(body, path, method, "application/json");
    }

    private HttpRequest buildHttpRequest(Object body, String path, String method, String accept) {
        HttpRequest.Builder builder = defaultHeader(accept)
                .timeout(config.getReadTimeout())
                .uri(URI.create(config.getBaseUrl() + path));
        if (body instanceof MultipartBodyRequest multipart) {
            // 非 JSON 请求体（如 multipart 上传文件）：使用请求提供的 body 与 Content-Type
            builder.header("Content-Type", multipart.contentType());
            return builder.method(method, HttpRequest.BodyPublishers.ofByteArray(multipart.body())).build();
        }
        String json;
        try {
            json = MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new DeepSeekException("序列化请求失败：" + e.getMessage(), e);
        }
        builder.header("Content-Type", "application/json");
        return builder.method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build();
    }

    @Override
    public <T extends BaseResponse> Mono<T> get(BaseRequest<T> request) {
        return http(request,"GET");
    }

    @Override
    public <T extends BaseResponse> Mono<T> post(BaseRequest<T> request) {
        return http(request,"POST");
    }

    /** 发送请求并按字节接收响应（用于二进制接口，如 TTS 音频）；非 200 时抛出带错误详情的异常。 */
    private Mono<HttpResponse<byte[]>> sendForBytes(BaseRequest<?> request) {
        // 二进制接口：Accept 不能是 application/json
        HttpRequest httpRequest = buildHttpRequest(request, request.getPath(), "POST", "*/*");
        return Mono.fromFuture(client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray()))
                .onErrorMap(e -> e instanceof DeepSeekException ? e : new DeepSeekException("接口异常：" + e.getMessage(), e))
                .flatMap(response -> {
                    if (response.statusCode() == 200) {
                        return Mono.just(response);
                    }
                    DeepSeekErrorEnum deepSeekErrorEnum = DeepSeekErrorEnum.fromCode(response.statusCode());
                    log.debug(deepSeekErrorEnum.desc);
                    // 错误响应是 JSON 文本
                    String reason = new String(response.body(), StandardCharsets.UTF_8);
                    String message = deepSeekErrorEnum.desc;
                    if (!reason.isBlank()) {
                        message += "。详情：" + reason;
                    }
                    return Mono.error(new DeepSeekException(message));
                });
    }

    @Override
    public Mono<byte[]> postForBytes(BaseRequest<?> request) {
        // defer：保证 sendAsync 在订阅时才发起
        return Mono.defer(() -> sendForBytes(request).map(HttpResponse::body));
    }

    @Override
    public Mono<TtsResponse> tts(TtsRequest request) {
        return Mono.defer(() -> {
            // 本地校验：text 非空且不超过上限
            request.validate();
            return sendForBytes(request).map(response -> {
                TtsResponse result = new TtsResponse();
                result.setAudio(response.body());
                result.setContentType(response.headers().firstValue("Content-Type").orElse(null));
                result.setFormat(request.getFormat());
                return result;
            });
        });
    }

    @Override
    public <T extends BaseResponse> Mono<T> delete(BaseRequest<T> request) {
        return http(request, "DELETE");
    }

    @Override
    public <T extends BaseResponse> Mono<T> http(BaseRequest<T> request, String method) {
        // defer：保证 sendAsync 在订阅时才发起，序列化异常也通过 onError 传播而非方法调用处抛出
        return Mono.defer(() -> {
            HttpRequest httpRequest = buildHttpRequest(request, request.getPath(), method);
            return Mono.fromFuture(client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString()))
                    .onErrorMap(e -> e instanceof DeepSeekException ? e : new DeepSeekException("接口异常：" + e.getMessage(), e))
                    .handle((response, sink) -> {
                        if (response.statusCode() == 200) {
                            try {
                                sink.next(MAPPER.readValue(response.body(), request.getResponseClass()));
                            } catch (JsonProcessingException e) {
                                sink.error(new DeepSeekException("解析响应失败：" + e.getMessage(), e));
                            }
                        } else {
                            DeepSeekErrorEnum deepSeekErrorEnum = DeepSeekErrorEnum.fromCode(response.statusCode());
                            log.debug(deepSeekErrorEnum.desc);
                            String reason = response.body();
                            String message = deepSeekErrorEnum.desc;
                            if (reason != null && !reason.isBlank()) {
                                message += "。详情：" + reason;
                            }
                            sink.error(new DeepSeekException(message));
                        }
                    });
        });
    }

    @Override
    public <T extends BaseStreamResponse> Flux<T> stream(BaseStreamRequest<T> request) {
        // defer：保证 sendAsync 在订阅时才发起，序列化异常也通过 onError 传播而非方法调用处抛出
        return Flux.defer(() -> {
            HttpRequest httpRequest = buildHttpRequest(request, request.getPath(), "POST");
            return Mono.fromFuture(client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()))
                    .onErrorMap(e -> e instanceof DeepSeekException ? e : new DeepSeekException("接口异常：" + e.getMessage(), e))
                    .flatMapMany(response -> {
                        if (response.statusCode() != 200) {
                            DeepSeekErrorEnum deepSeekErrorEnum = DeepSeekErrorEnum.fromCode(response.statusCode());
                            log.debug(deepSeekErrorEnum.desc);
                            return Flux.<T>error(new DeepSeekException(deepSeekErrorEnum.desc));
                        }
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                        // 格式 data:
                        // 结束格式 data: [DONE]
                        return Flux.<T>create(sink -> {
                            String line;
                            try {
                                while ((line = reader.readLine()) != null) {
                                    if (sink.isCancelled()) {
                                        break;
                                    }
                                    if ("data: [DONE]".equals(line)) {
                                        log.debug("all data is DONE");
                                        break;
                                    } else if (line.startsWith("data: ")) {
                                        String json = line.substring(6);
                                        sink.next(MAPPER.readValue(json, request.getResponseClass()));
                                    }
                                }
                                sink.complete();
                            } catch (IOException e) {
                                sink.error(new DeepSeekException("接口异常：" + e.getMessage(), e));
                            }
                        }).subscribeOn(Schedulers.boundedElastic())
                          .doFinally(sig -> {
                              try {
                                  reader.close();
                              } catch (IOException ignored) {
                                  // 关闭流时忽略异常
                              }
                          });
                    });
        });
    }

    @Override
    public Flux<ResponseStreamEvent> streamResponse(ResponseStreamRequest request) {
        // defer：保证 sendAsync 在订阅时才发起，序列化异常也通过 onError 传播而非方法调用处抛出
        return Flux.defer(() -> {
            HttpRequest httpRequest = buildHttpRequest(request, request.getPath(), "POST");
            return Mono.fromFuture(client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()))
                    .onErrorMap(e -> e instanceof DeepSeekException ? e : new DeepSeekException("接口异常：" + e.getMessage(), e))
                    .flatMapMany(response -> {
                        if (response.statusCode() != 200) {
                            DeepSeekErrorEnum deepSeekErrorEnum = DeepSeekErrorEnum.fromCode(response.statusCode());
                            log.debug(deepSeekErrorEnum.desc);
                            return Flux.<ResponseStreamEvent>error(new DeepSeekException(deepSeekErrorEnum.desc));
                        }
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                        // Responses 流式协议：命名 SSE 事件
                        //   event: <type>
                        //   data: <json>
                        // 终止事件：response.completed / response.incomplete / response.failed（无 data: [DONE]）
                        return Flux.<ResponseStreamEvent>create(sink -> {
                            try {
                                pumpEventStream(reader, sink);
                                sink.complete();
                            } catch (IOException e) {
                                sink.error(new DeepSeekException("接口异常：" + e.getMessage(), e));
                            }
                        }).subscribeOn(Schedulers.boundedElastic())
                          .doFinally(sig -> {
                              try {
                                  reader.close();
                              } catch (IOException ignored) {
                                  // 关闭流时忽略异常
                              }
                          });
                    });
        });
    }

    /**
     * 解析 Responses 命名 SSE 事件流：读取 event:/data: 行，将 event 类型注入到反序列化后的事件对象，
     * 遇终止事件（response.completed/incomplete/failed）后停止。
     * package-private 以便单测喂数据。
     */
    static void pumpEventStream(BufferedReader reader, FluxSink<ResponseStreamEvent> sink) throws IOException {
        String line;
        String currentEvent = null;
        while ((line = reader.readLine()) != null) {
            if (sink.isCancelled()) {
                break;
            }
            if (line.isEmpty()) {
                // 事件块分隔，重置当前事件类型
                currentEvent = null;
                continue;
            }
            if (line.startsWith("event:")) {
                currentEvent = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                String json = line.substring("data:".length()).trim();
                if (json.isEmpty()) {
                    continue;
                }
                ResponseStreamEvent event = MAPPER.readValue(json, ResponseStreamEvent.class);
                if (currentEvent != null) {
                    event.setType(currentEvent);
                }
                sink.next(event);
                if (isTerminalEvent(currentEvent)) {
                    break;
                }
            }
        }
    }

    private static boolean isTerminalEvent(String type) {
        return "response.completed".equals(type)
                || "response.incomplete".equals(type)
                || "response.failed".equals(type);
    }

    @Override
    public Flux<MessageStreamEvent> streamMessage(MessageStreamRequest request) {
        // defer：保证 sendAsync 在订阅时才发起
        return Flux.defer(() -> {
            HttpRequest httpRequest = buildHttpRequest(request, request.getPath(), "POST");
            return Mono.fromFuture(client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()))
                    .onErrorMap(e -> e instanceof DeepSeekException ? e : new DeepSeekException("接口异常：" + e.getMessage(), e))
                    .flatMapMany(response -> {
                        if (response.statusCode() != 200) {
                            DeepSeekErrorEnum deepSeekErrorEnum = DeepSeekErrorEnum.fromCode(response.statusCode());
                            log.debug(deepSeekErrorEnum.desc);
                            return Flux.<MessageStreamEvent>error(new DeepSeekException(deepSeekErrorEnum.desc));
                        }
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                        // Anthropic 命名 SSE：忽略 event: 行（data 里已带 type），以 message_stop 结束
                        return Flux.<MessageStreamEvent>create(sink -> {
                            String line;
                            try {
                                while ((line = reader.readLine()) != null) {
                                    if (sink.isCancelled()) {
                                        break;
                                    }
                                    if (!line.startsWith("data:")) {
                                        continue;
                                    }
                                    String json = line.substring("data:".length()).trim();
                                    if (json.isEmpty()) {
                                        continue;
                                    }
                                    MessageStreamEvent event = MAPPER.readValue(json, MessageStreamEvent.class);
                                    sink.next(event);
                                    if ("message_stop".equals(event.getType())) {
                                        break;
                                    }
                                }
                                sink.complete();
                            } catch (IOException e) {
                                sink.error(new DeepSeekException("接口异常：" + e.getMessage(), e));
                            }
                        }).subscribeOn(Schedulers.boundedElastic())
                          .doFinally(sig -> {
                              try {
                                  reader.close();
                              } catch (IOException ignored) {
                                  // 关闭流时忽略异常
                              }
                          });
                    });
        });
    }

    @Override
    public Flux<byte[]> ttsStream(Flux<String> textChunks, TtsStreamOptions options) {
        TtsStreamOptions opts = options != null ? options : TtsStreamOptions.pcm();
        URI uri = buildTtsStreamUri(opts);
        return Flux.create(sink -> {
            AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
            WebSocket.Listener listener = new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    webSocketRef.set(webSocket);
                    // 手动背压：每收到一帧都要再 request(1)
                    webSocket.request(1);
                    // 串行发送文本片段（sendText 未完成时不能发下一条），最后发送 finish_text
                    (textChunks == null ? Flux.<String>empty() : textChunks)
                            .concatMap(chunk -> sendWsText(webSocket, toTtsTextEvent(chunk)))
                            .concatWith(Mono.defer(() -> sendWsText(webSocket, "{\"event\":\"finish_text\"}")))
                            .subscribe(
                                    ignored -> { },
                                    error -> {
                                        if (!sink.isCancelled()) {
                                            sink.error(new DeepSeekException("TTS 文本发送失败：" + error.getMessage(), error));
                                        }
                                        webSocket.abort();
                                    });
                }

                @Override
                public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                    byte[] frame = new byte[data.remaining()];
                    data.get(frame);
                    if (!sink.isCancelled()) {
                        sink.next(frame);
                    }
                    webSocket.request(1);
                    return null;
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    handleTtsFinishEvent(sink, data.toString());
                    webSocket.request(1);
                    return null;
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    if (!sink.isCancelled()) {
                        sink.complete();
                    }
                    return null;
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    if (!sink.isCancelled()) {
                        sink.error(new DeepSeekException("TTS WebSocket 异常：" + error.getMessage(), error));
                    }
                }
            };

            client.newWebSocketBuilder()
                    .header("Authorization", "Bearer " + config.getAccessKey())
                    .connectTimeout(config.getConnectTimeout())
                    .buildAsync(uri, listener)
                    .whenComplete((webSocket, error) -> {
                        if (error != null && !sink.isCancelled()) {
                            sink.error(new DeepSeekException("TTS WebSocket 建立失败：" + error.getMessage(), error));
                        }
                    });

            sink.onCancel(() -> {
                WebSocket webSocket = webSocketRef.get();
                if (webSocket != null) {
                    webSocket.abort();
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    /** 把一段文本包装成 TTS WebSocket 的 text 事件 JSON。 */
    private static String toTtsTextEvent(String chunk) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("event", "text");
            node.put("text", chunk == null ? "" : chunk);
            return MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new DeepSeekException("TTS 文本序列化失败：" + e.getMessage(), e);
        }
    }

    /** 串行发送一条 WebSocket 文本消息。 */
    private static Mono<Void> sendWsText(WebSocket webSocket, String text) {
        return Mono.fromFuture(webSocket.sendText(text, true)).then();
    }

    /** 处理服务端文本事件：{"event":"finish","code":"success","message":""}。 */
    private static void handleTtsFinishEvent(FluxSink<byte[]> sink, String text) {
        JsonNode node;
        try {
            node = MAPPER.readTree(text);
        } catch (JsonProcessingException e) {
            return; // 非 JSON 文本事件忽略
        }
        if (!"finish".equals(node.path("event").asText(null))) {
            return;
        }
        String code = node.path("code").asText("success");
        String message = node.path("message").asText("");
        if ("success".equals(code)) {
            if (!sink.isCancelled()) {
                sink.complete();
            }
        } else if (!sink.isCancelled()) {
            sink.error(new DeepSeekException("TTS 合成失败：" + code
                    + (message.isBlank() ? "" : "（" + message + "）")));
        }
    }

    /** 构建 TTS WebSocket 的 URI：把 baseUrl 的 http(s) 换成 ws(s)，并拼上查询参数。 */
    private URI buildTtsStreamUri(TtsStreamOptions options) {
        String base = config.getBaseUrl();
        String wsBase = base.replaceFirst("^http", "ws");
        if (!wsBase.endsWith("/")) {
            wsBase += "/";
        }
        String query = "?voice_id=" + urlEncode(options.getVoiceId())
                + "&format=" + urlEncode(options.getFormat())
                + "&filter_markdown=" + (Boolean.TRUE.equals(options.getFilterMarkdown()) ? "true" : "false");
        return URI.create(wsBase + "v1/tools/tts" + query);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

}
