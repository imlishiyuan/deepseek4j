package cn.lishiyuan.deepseek.api.tool;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 语音识别响应。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/api/asr">语音识别 API</a>
 */
@Data
public class AsrResponse extends BaseResponse {

    /** 请求 ID */
    @JsonProperty("request_id")
    private String requestId;

    /** 识别出的文本 */
    @JsonProperty("text")
    private String text;

    /** 音频时长（毫秒） */
    @JsonProperty("duration_ms")
    private Integer durationMs;
}
