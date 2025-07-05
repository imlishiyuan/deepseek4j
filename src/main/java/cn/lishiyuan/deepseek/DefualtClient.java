package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;
import cn.lishiyuan.deepseek.config.Config;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import cn.lishiyuan.deepseek.e.DeepseekErrorEnum;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
public class DefualtClient implements Client {

    private final Config config;

    public DefualtClient(String accessKey) {
        this(new Config(accessKey));
    }

    public DefualtClient(String accessKey, String baseUrl) {
        this(new Config(accessKey,baseUrl));
    }

    public DefualtClient(Config config) {
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

    @Override
    public <T extends BaseResponse> T get(BaseRequest<T> request) {
        return http(request,"GET");
    }

    @Override
    public <T extends BaseResponse> T post(BaseRequest<T> request) {
        return http(request,"POST");
    }

    @Override
    public <T extends BaseResponse> T http(BaseRequest<T> request,String method) {
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .timeout(config.getReadTimeout())
                .method(method,HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .uri(URI.create(config.getBaseUrl() + request.getPath()))
                .build();

        try {
            HttpResponse<String> stringHttpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(stringHttpResponse.statusCode() == 200){
                String body = stringHttpResponse.body();
                return JSON.parseObject(body, request.getResponseClass());
            }
            DeepseekErrorEnum deepseekErrorEnum = DeepseekErrorEnum.fromCode(stringHttpResponse.statusCode());
            log.debug(deepseekErrorEnum.desc);
            throw new DeepSeekException(deepseekErrorEnum.desc);
        } catch (IOException |InterruptedException e) {
            throw new DeepSeekException("接口异常："+e.getMessage(),e);
        }
    }

    @Override
    public <T extends BaseStreamResponse> void stream(BaseStreamRequest<T> request, Consumer<T> consumer) {
        HttpRequest.Builder builder = defaultHeader();
        HttpRequest httpRequest = builder
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(request), StandardCharsets.UTF_8))
                .timeout(config.getReadTimeout())
                .uri(URI.create(config.getBaseUrl() + request.getPath()))
                .build();
        client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream()).thenAcceptAsync(response -> {
           if(response.statusCode() == 200){
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
                           T resp = JSON.parseObject(json, request.getResponseClass());
                           consumer.accept(resp);
                       }
                   }
               }catch (IOException e){
                   throw new DeepSeekException("接口异常："+e.getMessage(),e);
               }
           }else {
               DeepseekErrorEnum deepseekErrorEnum = DeepseekErrorEnum.fromCode(response.statusCode());
               log.debug(deepseekErrorEnum.desc);
               throw new DeepSeekException(deepseekErrorEnum.desc);
           }
        });
    }

}
