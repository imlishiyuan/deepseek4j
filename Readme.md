## Deepseek4j是一个deepseek的api客户端 ❤ ❤ ❤ 

### required java 17 要求 Java 17

### quick start

1. add maven dependency 添加依赖

```xml
<dependency>
    <groupId>cn.lishiyuan</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.0</version>
</dependency>
```
2. use deepseek4j 使用deepseek4j

you can use deepseek4j quickly by the following code

你可以通过以下代码快速的使用deepseek4j

```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);

    ChatRequest.Message systemMessage = new ChatRequest.Message();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequest.Message userMessage = new ChatRequest.Message();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequest.Message> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    ChatResponse chatResponse = client.chat(chatRequest);
    System.out.println(chatResponse.getChoices().get(0).getContent());
}
```

### API
1. list model 列出模型
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);
    ListModelResponse modelList = client.listModel();
}
```

2. balance 查询余额
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);
    BalanceInfoResponse balanceInfo = client.getBalanceInfo();
}
```
3. chat 聊天
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);

    ChatRequest.Message systemMessage = new ChatRequest.Message();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequest.Message userMessage = new ChatRequest.Message();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequest.Message> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    ChatResponse chatResponse = client.chat(chatRequest);
}
```

4. stream chat 聊天(stream)
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);

    ChatRequest.Message systemMessage = new ChatRequest.Message();
    systemMessage.setRole(RoleEnums.SYSTEM.code);
    systemMessage.setContent("You are a helpful assistant");

    ChatRequest.Message userMessage = new ChatRequest.Message();
    userMessage.setRole(RoleEnums.USER.code);
    userMessage.setContent("你好");

    List<ChatRequest.Message> messageList = List.of(systemMessage, userMessage);
    ChatRequest chatRequest = ChatRequest.create(messageList, ModelEnums.DEEPSEEK_CHAT.code);
    client.streamChat(chatRequest,chatResponse -> {
        // handle chatResponse
    });
}
```
5. FIM
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);

    FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
    FimResponse fimResponse = client.fim(fimRequest);
}
```
6. stream FIM
```java
public static void main(String[] args) {
    Client client = new DefualtClient(accessKey);

    FimRequest fimRequest = FimRequest.create("今天的风好大天气好冷", ModelEnums.DEEPSEEK_CHAT.code);
    client.streamFim(fimRequest,fimResponse -> {
        // handle fimResponse
    });
}
```

### License
待补充




