package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.BaseRequest;
import cn.lishiyuan.deepseek.api.BaseResponse;
import cn.lishiyuan.deepseek.api.BaseStreamRequest;
import cn.lishiyuan.deepseek.api.BaseStreamResponse;

import java.util.function.Consumer;


public interface Client {

    <T extends BaseResponse> T  http(BaseRequest<T> request,String method);

    <T extends BaseResponse> T  get(BaseRequest<T> request);

    <T extends BaseResponse> T  post(BaseRequest<T> request);

    <T extends BaseStreamResponse> void stream(BaseStreamRequest<T> request, Consumer<T> consumer);
}
