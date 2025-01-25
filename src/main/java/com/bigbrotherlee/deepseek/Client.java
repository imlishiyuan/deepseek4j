package com.bigbrotherlee.deepseek;

import com.bigbrotherlee.deepseek.request.ChatRequest;
import com.bigbrotherlee.deepseek.request.FimRequest;
import com.bigbrotherlee.deepseek.response.*;

import java.util.function.Consumer;


public interface Client {

    ChatResponse chat(ChatRequest request);

    void streamChat(ChatRequest request, Consumer<StreamChatResponse> consumer);

    FimResponse fim(FimRequest request);

    void streamFim(FimRequest request, Consumer<StreamFimResponse> consumer);


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
