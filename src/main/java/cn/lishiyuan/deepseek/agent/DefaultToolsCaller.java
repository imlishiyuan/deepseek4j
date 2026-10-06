package cn.lishiyuan.deepseek.agent;

import cn.lishiyuan.deepseek.api.chat.*;
import cn.lishiyuan.deepseek.config.enums.FuncParamEnums;
import cn.lishiyuan.deepseek.config.enums.ToolTypeEnums;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
public class DefaultToolsCaller implements ToolsCaller{

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            // 忽略 transient 字段（EmptyRequest 的 responseClass/path）
            .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
            // 响应中可能出现模型未定义的字段，忽略以避免反序列化失败
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            // 不序列化 null 值字段（与原 fastjson2 默认行为一致，请求体更紧凑）
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    private final List<Tool> tools = new ArrayList<>();

    private final Map<String, Tool> nameToolsMap = new HashMap<>();
    private final Map<String, Method> nameMethodMap = new HashMap<>();
    private final Map<Method,FunctionParameter> methodFunctionParameterMap = new HashMap<>();

    private final Map<Method, Object> methodObjectMap = new HashMap<>();
    private final Map<Method,List<String>> methodParamMap = new HashMap<>();

    public DefaultToolsCaller(List<Object> objects) {
        // 反射解析tool
        for (Object obj : objects) {
            Class<?> clazz = obj.getClass();
            List<Method> methods = MethodUtils.getMethodsListWithAnnotation(clazz,DefTool.class,true,false);
            for (Method method : methods) {
                String name = method.getName();
                DefTool annotation = method.getAnnotation(DefTool.class);
                String description = annotation.description();
                if(StringUtils.isNoneBlank(annotation.name())){
                    name = annotation.name();
                }

                Tool tool = new Tool();
                tool.setType(ToolTypeEnums.FUNCTION.code);
                ToolFunction toolFunction = new ToolFunction();
                toolFunction.setName(name);
                toolFunction.setDescription(description);
                FunctionParameter parameters = new FunctionParameter();
                parameters.setType(FuncParamEnums.OBJ.code);

                Map<String,FunctionParameter> properties = new HashMap<>();
                List<String> requiredList = new ArrayList<>();
                // 参数解析
                Parameter[] methodParameters = method.getParameters();
                for (Parameter methodParameter : methodParameters) {
                    DefParam param = methodParameter.getAnnotation(DefParam.class);
                    if (ObjectUtils.isEmpty(param)){
                        throw new IllegalArgumentException(name+" method parameter must have @DefParam annotation");
                    }
                    String paramName = param.name();
                    if(StringUtils.isBlank(paramName)){
                        throw new IllegalArgumentException(name+" method parameter`s @DefParam must set name");
                    }
                    String paramDesc = param.description();
                    // 用 ParameterizedType 构造 JavaType，才能拿到 List<Foo> / Map<K,V> 的泛型信息
                    JavaType javaType = MAPPER.getTypeFactory()
                            .constructType(methodParameter.getParameterizedType());
                    // 转换类型
                    FunctionParameter funcParamDefinition = convertFunctionParameter(javaType, paramDesc);
                    properties.put(paramName,funcParamDefinition);
                    requiredList.add(paramName);
                }

                if(!requiredList.isEmpty()){
                    parameters.setRequired(requiredList);
                    parameters.setProperties(properties);
                    toolFunction.setParameters(parameters);
                }

                // 处理好了映射关系
                tool.setFunction(toolFunction);
                tools.add(tool);
                Method old = nameMethodMap.putIfAbsent(name, method);
                if(!ObjectUtils.isEmpty(old)){
                    log.warn("duplicate tools  name:{}",name);
                }
                nameToolsMap.putIfAbsent(name, tool);
                methodObjectMap.putIfAbsent(method, obj);
                methodFunctionParameterMap.putIfAbsent(method,parameters);
                // 参数名顺序（按方法声明顺序），call() 时按它把 arguments 映射回方法参数
                methodParamMap.putIfAbsent(method, requiredList);
            }
        }
    }

    /**
     * 由 Java 类型递归生成参数的 JSON Schema（{@link FunctionParameter}）。
     * <p>基于 Jackson 的类型内省（{@code BeanDescription#findProperties}）而不是 BeanUtils：
     * <ul>
     *   <li>record / Lombok / 泛型都能正确识别（BeanUtils 只认 {@code getX()}，record 认不出来）；</li>
     *   <li>不会把 {@code getClass()} 当成属性（BeanUtils 会带出 {@code class}，递归到栈溢出）；</li>
     *   <li>数组 / 集合能补上 {@code items}（元素类型），否则模型不知道元素长什么样；</li>
     *   <li>带循环引用的类型不会无限展开（用 visiting 去重）。</li>
     * </ul>
     */
    private FunctionParameter convertFunctionParameter(JavaType javaType, String description) {
        return convertFunctionParameter(javaType, description, new HashSet<>());
    }

    private FunctionParameter convertFunctionParameter(JavaType javaType, String description, Set<Class<?>> visiting) {
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setDescription(description);

        Class<?> rawClass = javaType.getRawClass();
        FuncParamEnums kind = jsonType(rawClass);
        functionParameter.setType(kind.code);

        // 数组 / 集合：补 items
        if (kind == FuncParamEnums.ARR) {
            JavaType contentType = javaType.getContentType();
            if (contentType != null && contentType.getRawClass() != Object.class) {
                functionParameter.setItems(convertFunctionParameter(contentType, null, visiting));
            }
            return functionParameter;
        }

        // 对象则递归提取
        if (kind == FuncParamEnums.OBJ && !rawClass.isEnum()) {
            if (!visiting.add(rawClass)) {
                // 自引用 / 循环引用：不再展开，留一个 object 占位
                return functionParameter;
            }
            try {
                BeanDescription beanDescription = MAPPER.getSerializationConfig().introspect(javaType);
                Map<String, FunctionParameter> properties = new LinkedHashMap<>();
                List<String> requiredList = new ArrayList<>();

                for (BeanPropertyDefinition property : beanDescription.findProperties()) {
                    // 注意：schema 的键必须与 Jackson 反序列化时使用的属性名一致。
                    // 嵌套属性不能按 @DefParam.name 改名，否则模型照着 schema 传参、Jackson 却匹配不上，
                    // 值会静默丢失（record 尤其明显：record 只有组件名，没有 setter）。
                    String name = property.getName();
                    String desc = propertyDescription(rawClass, property, name);
                    properties.put(name, convertFunctionParameter(property.getPrimaryType(), desc, visiting));
                    requiredList.add(name);
                }

                if (!requiredList.isEmpty()) {
                    functionParameter.setProperties(properties);
                    functionParameter.setRequired(requiredList);
                }
            } finally {
                visiting.remove(rawClass);
            }
        }

        return functionParameter;
    }

    /**
     * 取嵌套属性的说明（{@code @DefParam.description}）。
     * <p>查找顺序：Jackson 暴露的字段 → primary member（getter）→ **按属性名反射找字段**。
     * 最后一步是为 record 准备的：Jackson 对 record 只暴露 accessor（{@code field}/{@code ctorParam} 都是 null），
     * 而 record 组件上的注解实际落在私有 final 字段上，只能靠普通反射拿到。
     * <p>属性名以 Jackson 的名字为准；若 {@code @DefParam.name} 与之不一致只告警，不做改名（改了会反序列化不上）。
     */
    private static String propertyDescription(Class<?> rawClass, BeanPropertyDefinition property, String name) {
        DefParam annotation = null;
        AnnotatedField field = property.getField();
        if (field != null) {
            annotation = field.getAnnotation(DefParam.class);
        }
        if (annotation == null && property.getPrimaryMember() != null) {
            annotation = property.getPrimaryMember().getAnnotation(DefParam.class);
        }
        if (annotation == null) {
            Field declared = findField(rawClass, name);
            if (declared != null) {
                annotation = declared.getAnnotation(DefParam.class);
            }
        }
        if (ObjectUtils.isEmpty(annotation)) {
            return null;
        }
        if (StringUtils.isNotBlank(annotation.name()) && !annotation.name().equals(name)) {
            log.warn("{}#{} 上的 @DefParam(name={}) 与 Jackson 属性名不一致，已忽略该 name（否则反序列化匹配不上）",
                    rawClass.getSimpleName(), name, annotation.name());
        }
        return annotation.description();
    }

    private static Field findField(Class<?> clazz, String name) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // 继续往父类找
            }
        }
        return null;
    }

    private FuncParamEnums jsonType(Class<?> clazz){
        if (clazz == String.class || clazz == Character.class || clazz == char.class || clazz.isEnum()) {
            return FuncParamEnums.STR;
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return FuncParamEnums.BOOL;
        }
        if (clazz == byte.class || clazz == Byte.class
                || clazz == short.class || clazz == Short.class
                || clazz == int.class || clazz == Integer.class
                || clazz == long.class || clazz == Long.class
                || clazz == BigInteger.class) {
            return FuncParamEnums.INT;
        }
        if (clazz == float.class || clazz == Float.class
                || clazz == double.class || clazz == Double.class
                || clazz == BigDecimal.class) {
            return FuncParamEnums.NUM;
        }
        if (clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            return FuncParamEnums.ARR;
        }
        return FuncParamEnums.OBJ;
    }

    @Override
    public List<Tool> tools() {
        return tools;
    }

    @Override
    public String call(ToolCall tool) {
        // 获取到对应的方法与参数列表
        ToolCallFunction function = tool.getFunction();
        String name = function.getName();
        Method method = nameMethodMap.get(name);
        if (ObjectUtils.isEmpty(method)){
            return "tool doesn't exist";
        }
        FunctionParameter functionParameter = methodFunctionParameterMap.get(method);
        Object object = methodObjectMap.get(method);

        Map<String, FunctionParameter> properties = functionParameter.getProperties();
        if (ObjectUtils.isEmpty(properties)){
            // 无需参数，表示不需要转换，直接执行获取结果
            try {
                Object result = method.invoke(object);
                return convertResult(result);
            } catch (JsonProcessingException e){
                return "call success but result can not write as string";
            } catch (IllegalAccessException | InvocationTargetException e) {
                return e.getMessage();
            }
        }

        // 转换参数：把 arguments 解析成一棵 JsonNode 树（保留原始结构），再按每个方法参数
        // 声明的「泛型类型」逐层转换。
        // 注意不能用 Map<String,String> 承接：Jackson 无法把嵌套对象塞进 String 值
        // （MismatchedInputException: Cannot deserialize value of type String from Object value）。
        String arguments = function.getArguments();

        try {
            JsonNode args = StringUtils.isBlank(arguments)
                    ? MAPPER.createObjectNode()
                    : MAPPER.readTree(arguments);
            List<String> nameOrder = methodParamMap.get(method);
            Type[] genericTypes = method.getGenericParameterTypes();
            Object[] objs = new Object[nameOrder.size()];
            for (int i = 0; i < nameOrder.size(); i++) {
                String paramName = nameOrder.get(i);
                JsonNode node = args.get(paramName);
                if (node == null || node.isNull()) {
                    // 缺失参数 -> null（工具参数建议用包装类型，自行处理 null）
                    objs[i] = null;
                    continue;
                }
                objs[i] = convertParam(node, genericTypes[i]);
            }

            Object result = method.invoke(object, objs);
            String s = convertResult(result);
            log.debug("call tool = {}; result = {}",tool,s);
            return s;
        } catch (JsonProcessingException e) {
            log.warn("tool {} 参数解析失败：{}", name, e.getMessage());
            return "call success but result can not write as string or your params can not be parsed";
        } catch (IllegalArgumentException e) {
            log.warn("tool {} 参数类型不匹配：{}", name, e.getMessage());
            return "call failed: " + e.getMessage();
        } catch (IllegalAccessException | InvocationTargetException e) {
            return e.getMessage();
        }
    }

    /**
     * 把一个 JsonNode 按目标（含泛型）类型转换。
     * <p>除了标准 JSON 结构，还容忍模型把对象/数组「字符串化」后传进来（例如 {@code "{\"x\":1}"}）——
     * 这是模型发起工具调用时很常见的输出形态。
     */
    private static Object convertParam(JsonNode node, Type type) throws JsonProcessingException {
        JavaType javaType = MAPPER.getTypeFactory().constructType(type);
        try {
            return MAPPER.convertValue(node, javaType);
        } catch (IllegalArgumentException e) {
            if (node.isTextual() && StringUtils.isNotBlank(node.asText())) {
                return MAPPER.readValue(node.asText(), javaType);
            }
            throw e;
        }
    }

    private String convertResult(Object obj) throws JsonProcessingException {
        // 直接json装换
        return MAPPER.writeValueAsString(obj);
    }

}
