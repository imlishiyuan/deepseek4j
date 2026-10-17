package cn.lishiyuan.deepseek.api.search;

import cn.lishiyuan.deepseek.api.BaseRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 网页抓取请求（POST /v1/tools/fetch）。
 * <p>抓取单个网页，返回页面标题与 Markdown 格式的正文。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/search">网页搜索与抓取</a>
 */
@Data
public class FetchRequest extends BaseRequest<FetchResponse> {

    /**
     * 要抓取的网页地址，长度不超过 8192；
     * 必须是含 host 的 HTTP/HTTPS URL，且不能携带用户名或密码。
     */
    @JsonProperty("url")
    private String url;

    public static FetchRequest create(String url) {
        FetchRequest request = new FetchRequest();
        request.setUrl(url);
        return request;
    }

    @Override
    public Class<FetchResponse> getResponseClass() {
        return FetchResponse.class;
    }

    @Override
    public String getPath() {
        return "v1/tools/fetch";
    }
}
