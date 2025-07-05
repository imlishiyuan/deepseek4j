package cn.lishiyuan.deepseek.config;

import lombok.Data;
import lombok.Setter;

import java.time.Duration;

@Data
public class Config {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/";
    private String baseUrl = DEFAULT_BASE_URL;
    private String accessKey;

    private Duration connectTimeout = Duration.ofSeconds(30);

    private Duration readTimeout = Duration.ofSeconds(60);

    public Config(){}

    public Config(String accessKey) {
        this.accessKey = accessKey;
    }
    public Config(String accessKey, String baseUrl) {
        this.accessKey = accessKey;
        this.baseUrl = baseUrl;
    }
}
