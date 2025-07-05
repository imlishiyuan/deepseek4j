package cn.lishiyuan.deepseek.api;


import cn.lishiyuan.deepseek.api.platform.BalanceInfoResponse;
import cn.lishiyuan.deepseek.api.platform.ListModelResponse;

public class EmptyRequest<T extends BaseResponse> extends BaseRequest<T>{

    private final transient Class<T> responseClass;

    private final transient String path;

    private EmptyRequest(Class<T> responseClass,String path) {
        this.responseClass = responseClass;
        this.path = path;
    }

    @Override
    public Class<T> getResponseClass() {
        return responseClass;
    }

    @Override
    public String getPath() {
        return path;
    }

    public static  EmptyRequest<BalanceInfoResponse> createBalanceRequest() {
        return new EmptyRequest<BalanceInfoResponse>(BalanceInfoResponse.class,"user/balance");
    }

    public static  EmptyRequest<ListModelResponse> createListModelRequest() {
        return new EmptyRequest<ListModelResponse>(ListModelResponse.class,"models");
    }

}
