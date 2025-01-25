package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.e.DeepSeekException;
import cn.lishiyuan.deepseek.request.ChatRequest;
import cn.lishiyuan.deepseek.request.FimRequest;
import cn.lishiyuan.deepseek.response.*;
import com.alibaba.fastjson2.JSON;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

@Slf4j
public class DefualtClient implements Client {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/";

    private final String accessKey;

    private final String baseUrl;



    public DefualtClient(String accessKey) {
        this(accessKey, DEFAULT_BASE_URL);

    }

    public DefualtClient(String accessKey, String baseUrl) {
        this.accessKey = accessKey;
        this.baseUrl = baseUrl;
        completeUrl();
        initClient();
    }

    /**
     * 补全API
     */

    private String chatUrl;

    private String fimUrl;

    private String listModelUrl;

    private String balanceInfoUrl;

    private void completeUrl() {
        this.chatUrl = this.baseUrl + "chat/completions";
        this.fimUrl = this.baseUrl + "beta/completions";
        this.listModelUrl = this.baseUrl + "models";
        this.balanceInfoUrl = this.baseUrl + "user/balance";
    }

    private HttpClient client;

    @Setter
    private Duration connectTimeout = Duration.ofSeconds(30);
    @Setter
    private Duration readTimeout = Duration.ofSeconds(60);


    private void initClient(){
        this.client = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
    }

    private HttpRequest.Builder defaultHeader(){
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        request.setStream(Boolean.FALSE);
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .timeout(readTimeout)
                .uri(URI.create(chatUrl))
                .build();

        try {
            HttpResponse<String> stringHttpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = stringHttpResponse.body();
            return JSON.parseObject(body, ChatResponse.class);
        } catch (IOException |InterruptedException e) {
            throw new DeepSeekException("接口异常："+e.getMessage(),e);
        }
    }

    @Override
    public void streamChat(ChatRequest request, Consumer<StreamChatResponse> consumer) {
        request.setStream(Boolean.TRUE);
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .timeout(readTimeout)
                .uri(URI.create(chatUrl))
                .build();
        client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).thenAcceptAsync(response -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
            // 格式 data:
            // 结束格式 data: [DONE]
            String line;
            try{
                while ((line = reader.readLine()) != null) {
                    if ("data: [DONE]".equals(line)) {
                        log.debug("all data is DONE");
                        break;
                    }else if (line.startsWith("data: ")) {
                        String json = line.substring(6);
                        StreamChatResponse chatResponse = JSON.parseObject(json, StreamChatResponse.class);
                        consumer.accept(chatResponse);
                    }
                }
            }catch (IOException e){
                throw new DeepSeekException("接口异常："+e.getMessage(),e);
            }
        });
    }

    @Override
    public FimResponse fim(FimRequest request) {
        request.setStream(Boolean.FALSE);
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .timeout(readTimeout)
                .uri(URI.create(fimUrl))
                .build();

        try {
            HttpResponse<String> stringHttpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = stringHttpResponse.body();
            return JSON.parseObject(body, FimResponse.class);
        } catch (IOException |InterruptedException e) {
            throw new DeepSeekException("接口异常："+e.getMessage(),e);
        }
    }

    @Override
    public void streamFim(FimRequest request, Consumer<StreamFimResponse> consumer) {
        request.setStream(Boolean.TRUE);
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .timeout(readTimeout)
                .uri(URI.create(fimUrl))
                .build();

        client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).thenAcceptAsync(response -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
            // 格式 data:
            // 结束格式 data: [DONE]
            String line;
            try{
                while ((line = reader.readLine()) != null) {
                    if ("data: [DONE]".equals(line)) {
                        log.debug("all data is DONE");
                        break;
                    }else if (line.startsWith("data: ")) {
                        String json = line.substring(6);
                        StreamFimResponse streamFimResponse = JSON.parseObject(json, StreamFimResponse.class);
                        consumer.accept(streamFimResponse);
                    }
                }
            }catch (IOException e){
                throw new DeepSeekException("接口异常："+e.getMessage(),e);
            }
        }).join();
    }

    @Override
    public ListModelResponse listModel() {
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .GET()
                .uri(URI.create(listModelUrl))
                .build();
        try {
            HttpResponse<String> stringHttpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = stringHttpResponse.body();
            return JSON.parseObject(body, ListModelResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new DeepSeekException("接口异常："+e.getMessage(),e);
        }
    }

    @Override
    public BalanceInfoResponse getBalanceInfo() {
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .GET()
                .uri(URI.create(balanceInfoUrl))
                .build();
        try {
            HttpResponse<String> stringHttpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = stringHttpResponse.body();
            return JSON.parseObject(body, BalanceInfoResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new DeepSeekException("接口异常："+e.getMessage(),e);
        }
    }
}
