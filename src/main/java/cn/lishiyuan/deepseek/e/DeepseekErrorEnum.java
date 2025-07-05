package cn.lishiyuan.deepseek.e;

public enum DeepseekErrorEnum {

    BAD_REQUEST(400, "格式错误", "请求体格式错误，请根据错误信息修改请求体"),
    UNAUTHORIZED(401, "认证失败", "API key 错误或缺失，请检查 API key 或创建新的 key"),
    PAYMENT_REQUIRED(402, "余额不足", "账号余额不足，请充值后再试"),
    UNPROCESSABLE_ENTITY(422, "参数错误", "请求参数错误，请根据错误信息修改参数"),
    TOO_MANY_REQUESTS(429, "请求限速", "请求速率（TPM/RPM）达到上限，请合理控制请求频率"),
    INTERNAL_SERVER_ERROR(500, "服务器故障", "服务器内部错误，请稍后重试或联系技术支持"),
    SERVICE_UNAVAILABLE(503, "服务器繁忙", "服务器负载过高，请稍后重试");

    public final int code;
    public final String name;  // 中文名称
    public final String desc;  // 中文描述（原因 + 解决方法）

    DeepseekErrorEnum(int code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static DeepseekErrorEnum fromCode(int code) {
        for (DeepseekErrorEnum error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return INTERNAL_SERVER_ERROR;
    }
}