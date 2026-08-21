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
    <version>1.0.6</version>
</dependency>
```

### 2. Use it

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
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

    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
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
    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
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

### 9. Multimodal / Vision

`deepseek-v4-flash-vision-exp` (a temporary model — pass the name as a plain string) accepts images in addition to text.
With multimodal enabled, a `user` message's `content` becomes an array of content blocks instead of a plain string.
Three block types are supported: `text` (text), `image_url` (external URL or base64 data URL), and `file`
(a `file_id` reference or inline `file_data`).

```java
String visionModel = "deepseek-v4-flash-vision-exp";

UserMessage message = new UserMessage();
message.setContent(
        ContentPart.text("What is in this image?"),
        ContentPart.imageUrl("https://example.com/image.jpg"));   // external URL
// or inline base64:  ContentPart.imageDataUrl("image/jpeg", base64);
// or file reference: ContentPart.file("file-api-xxxxxxxxxxxxxxxx");

ChatRequest chatRequest = ChatRequest.create(List.of(message), visionModel);
ChatResponse chatResponse = client.post(chatRequest).block();
System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
```

> Note: an external `image_url` must be publicly reachable and downloadable by the DeepSeek service (max 8192 chars).
> Links with anti-hotlinking or unreachable hosts cause the service to report an image download failure — use a base64
> data URL or a Files API `file_id` instead.

The Responses API also supports multimodal: images are carried in `input_image` content blocks
(`image_url` is a string, or `file_id` to reference an uploaded file — mutually exclusive; optional `detail`).

```java
InputItem item = new InputItem();
item.setRole("user");
item.setContent(List.of(
        ContentBlock.text("input_text", "Describe this image."),
        ContentBlock.inputImage("https://example.com/image.jpg", "low"))); // or ContentBlock.inputImageByFileId(fileId)

ResponseResult result = client.post(ResponseRequest.create(List.of(item), visionModel)).block();
```

### 10. Files API

Upload images and reuse them by `file_id` in multimodal requests (max 64 MiB per file, see the
[docs](https://api-docs.deepseek.com/zh-cn/guides/files_api/)).

```java
// Upload a file
byte[] imageBytes = Files.readAllBytes(Paths.get("image.jpg"));
UploadFileRequest upload = UploadFileRequest.create(imageBytes, "image.jpg");
FileObject file = client.post(upload).block();
String fileId = file.getId();   // file-api-xxxxxxxxxxxxxxxx

// List files
ListFilesRequest listReq = ListFilesRequest.create();
listReq.setLimit(50);
FileListResponse files = client.get(listReq).block();

// Retrieve a single file
FileObject info = client.get(RetrieveFileRequest.create(fileId)).block();

// Delete a file
DeleteFileResponse deleted = client.delete(DeleteFileRequest.create(fileId)).block();
```

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
