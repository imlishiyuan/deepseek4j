package com.bigbrotherlee.deepseek;

import com.bigbrotherlee.deepseek.request.ChatRequest;
import com.bigbrotherlee.deepseek.request.FimRequest;
import com.bigbrotherlee.deepseek.response.BalanceInfoResponse;
import com.bigbrotherlee.deepseek.response.ChatResponse;
import com.bigbrotherlee.deepseek.response.FimResponse;
import com.bigbrotherlee.deepseek.response.ListModelResponse;


public interface Client {

    ChatResponse chat(ChatRequest request);

    FimResponse fim(FimRequest request);

    /**
     * 列出模型
     * @return
     */
    ListModelResponse listModel();

    /**
     * 账户余额信息
     * @return
     */
    BalanceInfoResponse getBalanceInfo();
}
