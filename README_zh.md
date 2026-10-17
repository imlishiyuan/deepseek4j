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
    <version>1.0.8</version>
</dependency>
```

### 2. 使用 deepseek4j

```java
public static void main(String[] args) {
    Client client = new DefaultClient(accessKey);

    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);
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

    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);
    ChatResponse chatResponse = client.post(chatRequest).block();
    System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
}
```

### 4. 流式聊天

```java
public static void main(String[] args) {
    SystemMessage systemMessage = new SystemMessage();
    systemMessage.setContent("You are a helpful assistant");

    UserMessage userMessage = new UserMessage();
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_FLASH.code);

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
    FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_FLASH.code);
    FimResponse fimResponse = client.post(fimRequest).block();
    Assertions.assertNotNull(fimResponse, "fimResponse不应该为空");
}
```

### 6. 流式 FIM

```java
public static void main(String[] args) {
    StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_FLASH.code);
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
    ResponseRequest request = ResponseRequest.create("你好", ModelEnums.DEEPSEEK_FLASH.code);
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
    ResponseStreamRequest request = ResponseStreamRequest.create("你好", ModelEnums.DEEPSEEK_FLASH.code);
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

### 9. 多模态 / 图像理解

`deepseek-v4-flash-vision-exp`（临时模型，模型名直接以字符串传入）支持在文本之外输入图片。开启多模态后，
`user` 消息的 `content` 从纯字符串变为内容块数组，支持三种内容块：`text`（文本）、`image_url`（外部 URL 或
base64 data URL）、`file`（通过 Files API 上传后的 `file_id` 引用或 `file_data` 内联）。

```java
String visionModel = "deepseek-v4-flash-vision-exp";

UserMessage message = new UserMessage();
message.setContent(
        ContentPart.text("这张图片里有什么？"),
        ContentPart.imageUrl("https://example.com/image.jpg"));   // 外部 URL
// 或 base64 内联：ContentPart.imageDataUrl("image/jpeg", base64);
// 或文件引用：   ContentPart.file("file-api-xxxxxxxxxxxxxxxx");

ChatRequest chatRequest = ChatRequest.create(List.of(message), visionModel);
ChatResponse chatResponse = client.post(chatRequest).block();
System.out.println(chatResponse.getChoices().get(0).getMessage().getContent());
```

> 注：`image_url` 的外部链接需可被 DeepSeek 服务端公开访问并下载（最长 8192 字符）。某些带防盗链或不可达的链接会导致服务端返回图片下载失败，此时改用 base64 data URL 或 Files API 的 `file_id`。

Responses API 同样支持多模态：图片以 `input_image` 内容块承载（`image_url` 为字符串，或 `file_id` 引用文件，二者互斥；可选 `detail`）。

```java
InputItem item = new InputItem();
item.setRole("user");
item.setContent(List.of(
        ContentBlock.text("input_text", "描述这张图片。"),
        ContentBlock.inputImage("https://example.com/image.jpg", "low"))); // 或 ContentBlock.inputImageByFileId(fileId)

ResponseResult result = client.post(ResponseRequest.create(List.of(item), visionModel)).block();
```

### 10. Files API（文件接口）

用于上传图片并在多模态请求中以 `file_id` 复用（单文件最大 64 MiB，参考[文档](https://api-docs.deepseek.com/zh-cn/guides/files_api/)）。

```java
// 上传文件
byte[] imageBytes = Files.readAllBytes(Paths.get("image.jpg"));
UploadFileRequest upload = UploadFileRequest.create(imageBytes, "image.jpg");
FileObject file = client.post(upload).block();
String fileId = file.getId();   // file-api-xxxxxxxxxxxxxxxx

// 列出文件
ListFilesRequest listReq = ListFilesRequest.create();
listReq.setLimit(50);
FileListResponse files = client.get(listReq).block();

// 查询单个文件
FileObject info = client.get(RetrieveFileRequest.create(fileId)).block();

// 删除文件
DeleteFileResponse deleted = client.delete(DeleteFileRequest.create(fileId)).block();
```

### 11. 网页搜索与抓取

DeepSeek 自研搜索引擎提供的两个**只读**接口（Bearer 鉴权，按请求次数计费，Search 与 Fetch 各自独立限速）：

- **Search**：`POST /v1/tools/search`，按 query 搜索网页，返回结构化结果；
- **Fetch**：`POST /v1/tools/fetch`，抓取单个网页，返回页面标题与 Markdown 正文。

```java
// 网页搜索：query 里的 site: / -site: / "" / lang: / after: / before: 等语法会原样传给搜索服务
SearchRequest searchRequest = SearchRequest.create("DeepSeek V4 site:github.com lang:zh after:2025");
searchRequest.setCount(5);              // 1~50，默认 10
searchRequest.setNeedContent(true);     // 需要网页正文时打开（默认不含正文）

SearchResponse search = client.post(searchRequest).block();
for (SearchResponse.Result r : search.getResults()) {
    System.out.println(r.getTitle() + " -> " + r.getUrl());
    System.out.println(r.getSummary());           // 长摘要，适合喂给模型
    System.out.println(r.getContent());           // need_content=true 时的正文
    System.out.println(r.getRerankScore() + " / authority=" + r.getAuthority());
}

// 网页抓取：返回 Markdown 正文
FetchResponse page = client.post(FetchRequest.create("https://example.com/article")).block();
System.out.println(page.getTitle());
System.out.println(page.getContent());
```

- Search 的 `results` 是**空数组**表示"搜索正常但无结果"——不是错误，也不计费。
- Fetch 抓不到可用正文时返回 **404**（URL 不可用 / 被站点策略拦截 / 内容类型不支持等）；**403** 表示被安全策略拒绝。
- 两者都按请求次数计费（1000 次 5 元），`count` 只影响返回条数、不影响单价。
- 错误处理：**408 / 429 / 5xx 可等待后重试**；402 需先充值；其它 4xx 不改请求就不要重试。

### 12. Agent（简易 agent loop 示例）

`DefaultAgent` 是一个**最小实现**的 agent loop，用来演示"对话 + 工具调用"怎么组合，不是生产级框架。

```java
// 1) 用注解把普通方法定义成工具
public class MyTools {
    @DefTool(name = "get_weather", description = "查询城市天气")
    public Weather getWeather(@DefParam(name = "city", description = "城市名") String city) {
        return new Weather(city, 18);
    }
    public record Weather(String city, int temperatureC) { }
}

// 2) 配置并启动 agent
AgentConfig config = new AgentConfig("my-agent");
config.setSystemPrompt("你是助手");
config.setToolsCaller(new DefaultToolsCaller(List.of(new MyTools())));
Agent agent = new DefaultAgent(client, config);

// 3) 发消息：返回的 Mono 在该消息所属轮次结束时完成
UserMessage message = new UserMessage();
message.setContent("阿勒泰今天天气怎么样？");
ChatResponse response = agent.send(message, SendTimeEnums.NEXT_TURN).block();
System.out.println(response.getChoices().get(0).getMessage().getContent());
```

- **一轮（turn）** = 反复调用模型直到 `finish_reason` 不再是 `tool_calls`：模型要工具 → 本地执行 → 结果回灌 → 再请求，直到给出最终回答。
- **发送时机**：`SendTimeEnums.NEXT_REQUEST` 把消息并入当前轮的下一次模型请求（可打断正在进行的工具循环）；`NEXT_TURN` 等当前轮结束后作为下一轮开头。同一轮内被消费的消息共享该轮最终响应。
- 工具参数支持 `String`/包装类型/枚举/数组/`List`/`Map`/POJO/record（含嵌套），说明写在 `@DefParam` 上。
- 想看 agent 的请求与轮次 debug 日志：把 `cn.lishiyuan.deepseek.agent.request` 与 `.loop` 两个 logger 设为 DEBUG。

> ⚠️ 这是示例，不承诺生产可用：没有错误恢复（一次请求失败会终止 loop）、没有上下文压缩（`history` 无界增长）。

### 13. jar.md（随 jar 发布的 AI 使用说明）

本项目遵循 [jar.md 规范](https://github.com/imlishiyuan/jar.md)：把「给 AI agent 看的用法说明」随制品一起发布，agent 只凭 Maven 坐标就能确定性地发现并渐进加载它。

- **`META-INF/jar.md`**：内置在 jar 里的说明书（YAML frontmatter + 固定小节），随 jar 自动打包；
- **`deepseek4j-{version}-llms.txt`**：单独的门面构件（classifier `llms`），用于"这个库是不是我要的"的快速判断；
- 由 `jarmd-maven-plugin` 的 `attach-facade` 附加门面、`validate` 在 `verify` 阶段做规范一致性校验。

```bash
# 直接看说明书（前 40 行内是 frontmatter）
unzip -p deepseek4j-1.0.8.jar META-INF/jar.md | head -n 40
# 或抓门面
curl -O https://repo1.maven.org/maven2/cn/lishiyuan/deepseek4j/1.0.8/deepseek4j-1.0.8-llms.txt
```

## 许可证

本项目基于 [Apache License, Version 2.0](LICENSE) 开源。
