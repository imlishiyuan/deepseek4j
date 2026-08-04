# DeepSeek4j ❤ ❤ ❤

一个 DeepSeek API 的 Java 客户端。

**中文** | [English](README.md)

## 环境要求

- Java 17+

## 快速开始

### 1. 添加 Maven 依赖

![Maven Central Version](https://img.shields.io/maven-central/v/cn.lishiyuan/deepseek4j)

```xml
<dependency>
    <groupId>cn.lishiyuan</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.4</version>
</dependency>
```

### 2. 使用 deepseek4j

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
    ChatResponse chatResponse = client.post(chatRequest).block();
    System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
}
```

## API

### 1. 列出模型

```java
public static void main(String[] args) {
    ListModelResponse listModelResponse = client.get(EmptyRequest.createListModelRequest()).block();
    String name = listModelResponse.getData().stream()
            .map(ListModelResponse.Model::getId)
            .collect(Collectors.joining(","));
    System.out.println(name);
}
```

### 2. 查询余额

```java
public static void main(String[] args) {
    BalanceInfoResponse balanceInfo = client.get(EmptyRequest.createBalanceRequest()).block();
    Assertions.assertNotNull(balanceInfo, "balanceInfo不能为空");
}
```

### 3. 聊天

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
    ChatResponse chatResponse = client.post(chatRequest).block();
    System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
}
```

### 4. 流式聊天

```java
public static void main(String[] args) {
    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);

    client.stream(chatRequest)
            .doOnNext(chatResponse -> {
                // 处理每个增量响应
            })
            .blockLast();
}
```

### 5. FIM（补全中间内容）

```java
public static void main(String[] args) {
    FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_V4_FLASH.code);
    FimResponse fimResponse = client.post(fimRequest).block();
    Assertions.assertNotNull(fimResponse, "fimResponse不应该为空");
}
```

### 6. 流式 FIM

```java
public static void main(String[] args) {
    StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_V4_FLASH.code);
    client.stream(fimRequest)
            .doOnNext(fimResponse -> {
                // 处理每个增量响应
            })
            .blockLast();
}
```

### 7. responses（无状态，多轮需重发完整 input）

```java
public static void main(String[] args) {
    ResponseRequest request = ResponseRequest.create("你好", ModelEnums.DEEPSEEK_V4_FLASH.code);
    ResponseResult result = client.post(request).block();
    // 取首个 message 项的文本
    String text = result.getOutput().stream()
            .filter(o -> "message".equals(o.getType()))
            .flatMap(o -> o.getContent().stream())
            .map(ResponseResult.ContentBlock::getText)
            .collect(Collectors.joining());
    System.out.println(text);
}
```

### 8. stream responses（命名 SSE 事件，无 data: [DONE]）

```java
public static void main(String[] args) {
    ResponseStreamRequest request = ResponseStreamRequest.create("你好", ModelEnums.DEEPSEEK_V4_FLASH.code);
    client.streamResponse(request)
            .doOnNext(event -> {
                // 按事件类型处理增量
                if ("response.output_text.delta".equals(event.getType())) {
                    System.out.print(event.getDelta());
                }
            })
            .blockLast();
}
```

## 许可证

本项目基于 [Apache License, Version 2.0](LICENSE) 开源。
