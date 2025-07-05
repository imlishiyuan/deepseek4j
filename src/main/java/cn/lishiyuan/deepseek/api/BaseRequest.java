package cn.lishiyuan.deepseek.api;

import com.alibaba.fastjson2.annotation.JSONField;

public abstract class BaseRequest <T extends BaseResponse> {


    @JSONField(serialize = false)
    public abstract String getPath();

    @JSONField(serialize = false)
    public abstract Class<T> getResponseClass();
}
