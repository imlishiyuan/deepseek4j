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
    <version>1.0.8</version>
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
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);
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
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);
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
    StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);

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
    FimRequest fimRequest = FimRequest.create("It's so windy and cold today", ModelEnums.DEEPSEEK_FLASH.code);
    FimResponse fimResponse = client.post(fimRequest).block();
    Assertions.assertNotNull(fimResponse, "fimResponse must not be null");
}
```

### 6. Stream FIM

```java
public static void main(String[] args) {
    StreamFimRequest fimRequest = StreamFimRequest.create("It's so windy and cold today", ModelEnums.DEEPSEEK_FLASH.code);
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
    ResponseRequest request = ResponseRequest.create("Hello", ModelEnums.DEEPSEEK_FLASH.code);
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
    ResponseStreamRequest request = ResponseStreamRequest.create("Hello", ModelEnums.DEEPSEEK_FLASH.code);
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

### 11. Web Search & Fetch

Two **read-only** endpoints backed by DeepSeek's own search engine (Bearer auth, billed per request, Search and
Fetch are rate-limited independently):

- **Search**: `POST /v1/tools/search` — search the web by query, returning structured results;
- **Fetch**: `POST /v1/tools/fetch` — fetch a single page, returning its title and Markdown content.

```java
// Search: site: / -site: / "" / lang: / after: / before: syntax is passed through to the search service
SearchRequest searchRequest = SearchRequest.create("DeepSeek V4 site:github.com lang:zh after:2025");
searchRequest.setCount(5);              // 1..50, default 10
searchRequest.setNeedContent(true);     // set true to get page content (off by default)

SearchResponse search = client.post(searchRequest).block();
for (SearchResponse.Result r : search.getResults()) {
    System.out.println(r.getTitle() + " -> " + r.getUrl());
    System.out.println(r.getSummary());          // long summary, meant for models
    System.out.println(r.getContent());          // page content when need_content=true
    System.out.println(r.getRerankScore() + " / authority=" + r.getAuthority());
}

// Fetch: returns Markdown content
FetchResponse page = client.post(FetchRequest.create("https://example.com/article")).block();
System.out.println(page.getTitle());
System.out.println(page.getContent());
```

- An empty `results` array in Search means "the search ran fine but found nothing" — not an error, and not billed.
- Fetch returns **404** when no usable content can be extracted (unreachable URL, blocked by the site, unsupported
  content type); **403** means it was rejected by a safety policy.
- Both are billed per request (5 CNY per 1,000 calls); `count` only controls how many results come back.
- Errors: retry **408 / 429 / 5xx** after a wait; top up on 402; do not retry other 4xx without changing the request.

### 12. Agent (a minimal agent-loop example)

`DefaultAgent` is a **minimal** agent loop showing how to combine chat with tool calling — an example, not a
production-grade framework.

```java
// 1) Expose plain methods as tools with annotations
public class MyTools {
    @DefTool(name = "get_weather", description = "Get the weather of a city")
    public Weather getWeather(@DefParam(name = "city", description = "City name") String city) {
        return new Weather(city, 18);
    }
    public record Weather(String city, int temperatureC) { }
}

// 2) Configure and start the agent
AgentConfig config = new AgentConfig("my-agent");
config.setSystemPrompt("You are a helpful assistant.");
config.setToolsCaller(new DefaultToolsCaller(List.of(new MyTools())));
Agent agent = new DefaultAgent(client, config);

// 3) Send a message; the returned Mono completes when its turn ends
UserMessage message = new UserMessage();
message.setContent("What's the weather in Altay today?");
ChatResponse response = agent.send(message, SendTimeEnums.NEXT_TURN).block();
System.out.println(response.getChoices().get(0).getMessage().getContent());
```

- A **turn** = keep calling the model until `finish_reason` is no longer `tool_calls`: model asks for tools →
  run them locally → feed the results back → call again, until a final answer is produced.
- **Send timing**: `SendTimeEnums.NEXT_REQUEST` folds a message into the current turn's next model request
  (it can interrupt a running tool loop); `NEXT_TURN` starts the next turn after the current one finishes.
  All messages consumed by a turn share that turn's final response.
- Tool parameters may be `String`/wrappers/enums/arrays/`List`/`Map`/POJOs/records (nested included), described
  via `@DefParam`.
- To see per-request / per-turn debug logs, set the `cn.lishiyuan.deepseek.agent.request` and
  `cn.lishiyuan.deepseek.agent.loop` loggers to DEBUG.

> ⚠️ This is an example with no production guarantee: no error recovery (a single failed request terminates the
> loop) and no context compaction (`history` grows unbounded).

### 13. jar.md (AI-facing docs shipped with the JAR)

This project follows the [jar.md specification](https://github.com/imlishiyuan/jar.md): usage documentation aimed at
AI agents ships together with the artifact, so an agent can discover and progressively load it from the Maven
coordinates alone.

- **`META-INF/jar.md`**: the manual embedded in the jar (YAML frontmatter + fixed sections), packaged automatically;
- **`deepseek4j-{version}-llms.txt`**: a standalone facade artifact (classifier `llms`) for a quick "is this the
  library I need" check;
- `jarmd-maven-plugin` attaches the facade (`attach-facade`) and validates spec conformance during `verify`.

```bash
# Read the manual (frontmatter is within the first 40 lines)
unzip -p deepseek4j-1.0.8.jar META-INF/jar.md | head -n 40
# Or fetch the facade
curl -O https://repo1.maven.org/maven2/cn/lishiyuan/deepseek4j/1.0.8/deepseek4j-1.0.8-llms.txt
```

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
