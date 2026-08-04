package cn.lishiyuan.deepseek.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseStreamRequest<T extends BaseStreamResponse> {

    @JsonIgnore
    public abstract String getPath();

    @JsonIgnore
    public abstract Class<T> getResponseClass();
}
