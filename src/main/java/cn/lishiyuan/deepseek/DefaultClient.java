package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import cn.lishiyuan.deepseek.api.response.ResponseStreamEvent;
import cn.lishiyuan.deepseek.api.response.ResponseStreamRequest;
import cn.lishiyuan.deepseek.config.Config;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import cn.lishiyuan.deepseek.e.DeepSeekErrorEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.nio.charset.StandardCharsets;

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

    private HttpRequest.Builder defaultHeader(){
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + config.getAccessKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private HttpRequest buildHttpRequest(Object body, String path, String method) {
        String json;
        try {
            json = MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new DeepSeekException("序列化请求失败：" + e.getMessage(), e);
        }
        return defaultHeader()
                .timeout(config.getReadTimeout())
                .method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .uri(URI.create(config.getBaseUrl() + path))
                .build();
    }

    @Override
    public <T extends BaseResponse> Mono<T> get(BaseRequest<T> request) {
        return http(request,"GET");
    }

    @Override
    public <T extends BaseResponse> Mono<T> post(BaseRequest<T> request) {
        return http(request,"POST");
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
                            sink.error(new DeepSeekException(deepSeekErrorEnum.desc));
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

}
