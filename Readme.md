## Deepseek4j是一个deepseek的api客户端 ❤ ❤ ❤ 

### required java 17 要求 Java 17

### quick start

1. add maven dependency 添加依赖

![Maven Central Version](https://img.shields.io/maven-central/v/cn.lishiyuan/deepseek4j)


```xml
<dependency>
    <groupId>cn.lishiyuan</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.2</version>
</dependency>
```
2. use deepseek4j 使用deepseek4j

you can use deepseek4j quickly by the following code

你可以通过以下代码快速的使用deepseek4j

```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);
    
    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    ChatResponse chatResponse = client.post(chatRequest);
    System.out.println(chatResponse.getChoices().get(0).getContent());
}
```

### API
1. list model 列出模型

```java
public static void main(String[] args) {
    ListModelResponse listModelResponse = client.get(EmptyRequest.createListModelRequest());
    String name = listModelResponse.getData().stream().map(ListModelResponse.Model::getId).collect(Collectors.joining(","));
    System.out.println(name);
}
```

2. balance 查询余额

```java
public static void main(String[] args) {
    BalanceInfoResponse balanceInfo = client.get(EmptyRequest.createBalanceRequest());
    Assertions.assertNotNull(balanceInfo,"balanceInfo不能为空");
}
```

3. chat 聊天

```java
public static void main(String[] args) {

    Client client = new DefualtClient(accessKey);

    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    ChatResponse chatResponse = client.post(chatRequest);
    System.out.println(chatResponse.getChoices().get(0).getContent());
    
}
```

4. stream chat 聊天(stream)

```java
public static void main(String[] args) {
    ChatRequestMessage systemMessage = new ChatRequestMessage();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequestMessage userMessage = new ChatRequestMessage();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequestMessage> messageList = List.of(systemMessage, userMessage);
    StreamChatRequest chatRequest = StreamChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    CountDownLatch latch = new CountDownLatch(1);

    client.stream(chatRequest,chatResponse -> {
        
        
    });
}
```

5. FIM

```java
public static void main(String[] args) {
    FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
    FimResponse fimResponse = client.post(fimRequest);
    Assertions.assertNotNull(fimResponse,"fimResponse不应该为空");
}
```

6. stream FIM

```java
public static void main(String[] args) {
    CountDownLatch latch = new CountDownLatch(1);

    StreamFimRequest fimRequest = StreamFimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
    client.stream(fimRequest, fimResponse -> {
       
    });
}
```

### License
待补充




