package cn.lishiyuan.deepseek.e;

public enum DeepSeekErrorEnum {

    BAD_REQUEST(400, "格式错误", "请求体格式错误，请根据错误信息修改请求体"),
    UNAUTHORIZED(401, "认证失败", "API key 错误或缺失，请检查 API key 或创建新的 key"),
    PAYMENT_REQUIRED(402, "余额不足", "账号余额不足，请充值后再试"),
    FORBIDDEN(403, "无权限", "请求被安全策略拒绝（例如 Fetch 的目标站点不允许抓取）"),
    NOT_FOUND(404, "内容不可用", "未获取到可用内容：URL 不可用、被站点策略拦截、目标站点返回错误、内容类型不支持，或页面无可提取的正文"),
    REQUEST_TIMEOUT(408, "请求超时", "读取请求体超时，请稍后重试"),
    PAYLOAD_TOO_LARGE(413, "请求体过大", "请求体超过大小限制，请减少请求体体积"),
    UNPROCESSABLE_ENTITY(422, "参数错误", "请求参数错误，请根据错误信息修改参数"),
    TOO_MANY_REQUESTS(429, "请求限速", "请求速率达到上限（模型为 TPM/RPM，Search/Fetch 为 QPS），请合理控制请求频率"),
    INTERNAL_SERVER_ERROR(500, "服务器故障", "服务器内部错误，请稍后重试或联系技术支持"),
    SERVICE_UNAVAILABLE(503, "服务器繁忙", "服务器负载过高，请稍后重试"),
    GATEWAY_TIMEOUT(504, "网关超时", "下游请求超时，请稍后重试");

    public final int code;
    public final String name;  // 中文名称
    public final String desc;  // 中文描述（原因 + 解决方法）

    DeepSeekErrorEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static DeepSeekErrorEnum fromCode(int code) {
        for (DeepSeekErrorEnum error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}