# DeepSeek4j ❤ ❤ ❤

A Java client for the DeepSeek API.

**English** | [中文](README_zh.md)

## Requirements

- Java 17+

## Quick Start

### 1. Add the Maven dependency

![Maven Central Version](https://img.shields.io/maven-central/v/cn.lishiyuan/deepseek4j)

```xml
<dependency>
    <groupId>cn.lishiyuan</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.4</version>
</dependency>
```

### 2. Use it

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("Hello");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
    ChatResponse chatResponse = client.post(chatRequest).block();
    System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
}
```

## API

### 1. List models

```java
public static void main(String[] args) {
    ListModelResponse listModelResponse = client.get(EmptyRequest.createListModelRequest()).block();
    String name = listModelResponse.getData().stream()
            .map(ListModelResponse.Model::getId)
            .collect(Collectors.joining(","));
    System.out.println(name);
}
```

### 2. Query balance

```java
public static void main(String[] args) {
    BalanceInfoResponse balanceInfo = client.get(EmptyRequest.createBalanceRequest()).block();
    Assertions.assertNotNull(balanceInfo, "balanceInfo must not be null");
}
```

### 3. Chat

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("Hello");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);
    ChatResponse chatResponse = client.post(chatRequest).block();
    System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
}
```

### 4. Stream chat

```java
public static void main(String[] args) {
    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("Hello");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_V4_FLASH.code);

    client.stream(chatRequest)
            .doOnNext(chatResponse -> {
                // handle each incremental chunk
            })
            .blockLast();
}
```

### 5. FIM (Fill in the Middle)

```java
public static void main(String[] args) {
    FimRequest fimRequest = FimRequest.create("It's so windy and cold today", ModelEnums.DEEPSEEK_V4_FLASH.code);
    FimResponse fimResponse = client.post(fimRequest).block();
    Assertions.assertNotNull(fimResponse, "fimResponse must not be null");
}
```

### 6. Stream FIM

```java
public static void main(String[] args) {
    StreamFimRequest fimRequest = StreamFimRequest.create("It's so windy and cold today", ModelEnums.DEEPSEEK_V4_FLASH.code);
    client.stream(fimRequest)
            .doOnNext(fimResponse -> {
                // handle each incremental chunk
            })
            .blockLast();
}
```

### 7. Responses (stateless — resend the full input for multi-turn)

```java
public static void main(String[] args) {
    ResponseRequest request = ResponseRequest.create("Hello", ModelEnums.DEEPSEEK_V4_FLASH.code);
    ResponseResult result = client.post(request).block();
    // extract the text of the first message output item
    String text = result.getOutput().stream()
            .filter(o -> "message".equals(o.getType()))
            .flatMap(o -> o.getContent().stream())
            .map(ResponseResult.ContentBlock::getText)
            .collect(Collectors.joining());
    System.out.println(text);
}
```

### 8. Stream responses (named SSE events, no `data: [DONE]`)

```java
public static void main(String[] args) {
    ResponseStreamRequest request = ResponseStreamRequest.create("Hello", ModelEnums.DEEPSEEK_V4_FLASH.code);
    client.streamResponse(request)
            .doOnNext(event -> {
                // handle deltas by event type
                if ("response.output_text.delta".equals(event.getType())) {
                    System.out.print(event.getDelta());
                }
            })
            .blockLast();
}
```

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
