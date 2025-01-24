package com.bigbrotherlee.deepseek;

import com.alibaba.fastjson2.JSON;
import com.bigbrotherlee.deepseek.e.DeepSeekException;
import com.bigbrotherlee.deepseek.request.ChatRequest;
import com.bigbrotherlee.deepseek.request.FimRequest;
import com.bigbrotherlee.deepseek.response.BalanceInfoResponse;
import com.bigbrotherlee.deepseek.response.ChatResponse;
import com.bigbrotherlee.deepseek.response.FimResponse;
import com.bigbrotherlee.deepseek.response.ListModelResponse;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

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
    public ChatResponse chat(ChatRequest chatRequest) {
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(chatRequest), StandardCharsets.UTF_8))
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
    public FimResponse fim(FimRequest request) {
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
