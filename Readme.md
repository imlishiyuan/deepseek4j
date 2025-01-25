## Deepseek4j是一个deepseek的api客户端 ❤ ❤ ❤ 

### quick start

#### 1. add dependency

```xml
<dependency>
    <groupId>com.deepseek</groupId>
    <artifactId>deepseek4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

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




