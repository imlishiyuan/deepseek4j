package cn.lishiyuan.deepseek.api;

import com.alibaba.fastjson2.annotation.JSONField;

public abstract class BaseStreamRequest<T extends BaseStreamResponse> {

    @JSONField(serialize = false)
    public abstract String getPath();

    @JSONField(serialize = false)
    public abstract Class<T> getResponseClass();
}
