package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.chat.ChatResponse;
import cn.lishiyuan.deepseek.api.chat.msg.UserMessage;
import cn.lishiyuan.deepseek.config.AgentConfig;
import cn.lishiyuan.deepseek.config.enums.SendTimeEnums;
import reactor.core.publisher.Mono;

/**
 * 一个见简易的agent接口，定义了如何处理每个轮次
 */
public interface Agent {

    void configChange(AgentConfig config);

    Mono<ChatResponse> send(UserMessage userMessage, SendTimeEnums sendTime);
}
