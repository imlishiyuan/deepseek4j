---
spec: jar.md/0.0.1
groupId: cn.lishiyuan
artifactId: deepseek4j
version: 1.0.7
name: DeepSeek4j
summary: DeepSeek API 的 Java 客户端：对话/流式、多模态图像、Files API、Responses API 与简易 agent loop。
jdk: 17+
license: Apache-2.0
site: https://github.com/imlishiyuan/deepseek4j
status: stable
---

# DeepSeek4j

## Overview

DeepSeek API 的 Java 客户端。基于 Reactor（`Mono`/`Flux`）+ JDK 17 的 `java.net.http.HttpClient`，不引入额外 HTTP 框架。

设计要点：
1. **薄封装**：请求/响应对象与官方 API 字段一一对应，序列化交给 Jackson，没有自造 DSL。
2. **全异步**：所有调用返回 `Mono`/`Flux`；流式用 `Flux` 表达，Responses 用命名 SSE 事件。
3. **示例优先**：`DefaultAgent` 刻意保持最小实现，是"把 Chat + 工具调用组合起来"的范例，不是生产级 Agent 框架。

适用：在 Java 里直接调 DeepSeek（含多模态与文件上传），或想要一个能自己改的 agent loop 起点。
不适用：需要记忆压缩、多 agent 编排、会话持久化、失败重试策略的生产场景——这些要在 `DefaultAgent` 之上自己补。

## Quick Start

```xml
<dependency>
    <groupId>cn.lishiyuan</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.7</version>
</dependency>
```

```java
Client client = new DefaultClient(accessKey);

UserMessage userMessage = new UserMessage();
userMessage.setContent("你好");

ChatRequest request = ChatRequest.create(List.of(userMessage), ModelEnums.DEEPSEEK_FLASH.code);
ChatResponse response = client.post(request).block();
System.out.println(response.getChoices().get(0).getMessage().getContent());
```

`.block()` 只适合 main/测试；在 WebFlux 等响应式上下文里应继续用 `flatMap` 组合。

## Compatibility

| 项 | 要求 |
|---|---|
| JDK | 17+ |
| Reactor | 3.x（只需 `reactor-core`，不依赖 Reactor Netty） |
| Jackson | 2.17 |
| SLF4J | 2.0（运行时可无 provider，只是不输出日志） |

## Sections

- [API Reference](#api-reference) — 需要入口类与方法语义时加载
- [Configuration](#configuration) — 需要调参（thinking / max_tokens / tools）时加载
- [Advanced Usage](#advanced-usage) — 需要多模态、Files API 或 agent loop 时加载
- [Gotchas](#gotchas) — 排查踩坑时加载（本文件最有价值的一节）

## API Reference

### 入口类

| 类 | 职责 | 复杂度 |
|---|---|---|
| `Client` / `DefaultClient` | HTTP 入口：`get` / `post` / `delete` / `stream` / `streamResponse` | 低 |
| `ChatRequest` / `ChatResponse` | 对话补全（非流式） | 低 |
| `StreamChatRequest` | 对话补全（流式，`Flux<StreamChatResponse>`） | 低 |
| `ResponseRequest` / `ResponseStreamRequest` | Responses API（无状态，多轮需重发完整 input） | 中 |
| `UploadFileRequest` / `ListFilesRequest` / `RetrieveFileRequest` / `DeleteFileRequest` | Files API | 低 |
| `Agent` / `DefaultAgent` | 简易 agent loop（工具调用循环） | 中 |
| `DefaultToolsCaller` + `@DefTool` / `@DefParam` | 把普通 Java 方法暴露成工具 | 中 |

### 关键方法

- `Client.post(BaseRequest)`：非流式调用。请求对象自带 `getPath()` 与 `getResponseClass()`，所以同一个 `post` 能发 chat / fim / responses / files。
- `Client.stream(BaseStreamRequest)`：读 `data: ...` + `data: [DONE]` 的 SSE。
- `Client.streamResponse(...)`：读 Responses 的命名事件（`event:` + `data:`），**没有** `[DONE]`。
- `Agent.send(UserMessage, SendTimeEnums)`：把消息投给 agent loop；返回的 `Mono<ChatResponse>` 在**该消息所属轮次结束**时完成。
- `Client.delete(BaseRequest)`：DELETE（Files API 删除文件用）。

## Configuration

`AgentConfig`（`DefaultAgent` 用）：

| 键 | 默认 | 作用 |
|---|---|---|
| `systemPrompt` | null | 每次请求动态前置，改后立即生效 |
| `defaultSendTime` | `NEXT_TURN` | `send(msg, null)` 时的默认时机 |
| `thinking` / `thinkingEffort` | `ENABLED` / `HIGH` | 思考模式与推理强度 |
| `model` | `DEEPSEEK_FLASH` | 决定请求的 `model` 字段 |
| `toolsCaller` | null | 为 null 时不带 tools（会跳过，不 NPE） |
| `maxTokens` / `temperature` / `topP` / `stop` / `toolChoice` | — | 透传给 `ChatRequest` |

`Config`（`DefaultClient` 用）：`baseUrl`、`accessKey`、`connectTimeout`、`readTimeout`。

## Advanced Usage

### 多模态（图像）

`user` 消息的 `content` 既可以是字符串，也可以是内容块数组（`text` / `image_url` / `file`）：

```java
UserMessage message = new UserMessage();
message.setContent(
        ContentPart.text("这张图片里有什么？"),
        ContentPart.imageUrl("https://example.com/image.jpg"),
        ContentPart.file("file-api-xxxxxxxxxxxxxxxx"));
```

### Files API

```java
FileObject file = client.post(UploadFileRequest.create(bytes, "image.jpg")).block();
String fileId = file.getId();
```

### Agent loop

```java
AgentConfig config = new AgentConfig("my-agent");
config.setSystemPrompt("你是助手");
config.setToolsCaller(new DefaultToolsCaller(List.of(new MyTools())));
Agent agent = new DefaultAgent(client, config);

ChatResponse response = agent.send(message, SendTimeEnums.NEXT_TURN).block();
```

工具就是一个带注解的普通方法：

```java
public class MyTools {
    @DefTool(name = "get_weather", description = "查询城市天气")
    public Weather getWeather(@DefParam(name = "city", description = "城市名") String city) {
        return new Weather(city, 18);
    }
    public record Weather(String city, int temperatureC) { }
}
```

`SendTimeEnums.NEXT_REQUEST` 把消息并入**当前轮的下一次模型请求**（可打断工具循环）；`NEXT_TURN` 等当前轮结束后作为下一轮开头。同一轮内被消费的消息共享该轮最终响应。

## Gotchas

1. **外部图片链接必须能被 DeepSeek 服务端下载**。症状：返回 `Failed to download image from <url>`。原因：防盗链 / 超时 / 死链，服务端拉不到——不是请求体拼错。正确做法：改用 base64 data URL，或先 `UploadFileRequest` 再用 `file_id`。
2. **图片只给视觉模型，且只能在 `user` 消息里**。症状：其他模型返回 400 `This model does not support image`；system/assistant 带图也 400。
3. **`DefaultAgent` 没有错误恢复**。症状：某次 `client.post` 失败后整个 loop 终止，其后所有 `send` 的 `Mono` 永不完成。正确做法：在 `DefaultAgent` 外包一层重试，或自建 loop。
4. **`history` 无界增长**。`DefaultAgent` 不做上下文压缩，长会话 token 成本持续上涨；需要自己截断或摘要。
5. **工具参数名必须与 Jackson 的反序列化名一致**。嵌套属性不要用 `@DefParam.name` 改名：schema 会告诉模型新名字，但 Jackson 按原属性名绑定，值会静默丢失（record 尤其明显，它没有 setter）。
6. **工具参数用包装类型，不要用基本类型**。所有 `@DefParam` 参数都被标为 required；缺参时该位置传 `null`，基本类型会直接抛 `IllegalArgumentException`。
7. **`Mono` 是冷的**。不订阅就不会发请求；`.block()` 真会阻塞，响应式链路里请用 `flatMap` / `concatMap`。

## Migration Guide

### 1.0.6 → 1.0.7

1. 新增示例级 agent loop：`DefaultAgent` / `Agent` / `AgentConfig` / `DefaultToolsCaller` + `@DefTool` / `@DefParam`。
2. 请求 / 轮次 debug 日志：logger `cn.lishiyuan.deepseek.agent.request` 与 `.loop`。
3. 随制品发布 jar.md 文档（`META-INF/jar.md` 说明书 + `-llms.txt` 门面）。
4. 工具参数支持 record / 嵌套 POJO / 数组 / 集合（数组与集合的 schema 会生成 `items`）。

### 1.0.5 → 1.0.6

1. 多模态：`ChatRequestMessage.content` 由 `String` 改为 `Object`（`String` 或 `List<ContentPart>`）；旧的 `setContent("...")` 仍可用。
2. 新增 Files API（`cn.lishiyuan.deepseek.api.file.*`）与 `Client.delete(BaseRequest)`。
3. `ModelEnums` 收敛为 `DEEPSEEK_FLASH` / `DEEPSEEK_V4_PRO`。
