package cn.lishiyuan.deepseek;

import cn.lishiyuan.deepseek.api.search.FetchRequest;
import cn.lishiyuan.deepseek.api.search.FetchResponse;
import cn.lishiyuan.deepseek.api.search.SearchRequest;
import cn.lishiyuan.deepseek.api.search.SearchResponse;
import cn.lishiyuan.deepseek.e.DeepSeekException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 网页搜索（/v1/tools/search）与网页抓取（/v1/tools/fetch）的集成测试。
 * <p>会真实调用 DeepSeek API 并按次计费（Search / Fetch 各 5 QPS、1000 次 5 元），请按需运行。
 */
@DisplayName("Search / Fetch 集成测试")
public class SearchFetchClientTests {

    private static Client client;

    @BeforeAll
    public static void initEnv(){
        // read ackey
        Path path = Paths.get(SearchFetchClientTests.class.getClassLoader().getResource("accessKey.txt").getPath());
        try {
            String accessKey = Files.readString(path);
            client = new DefaultClient(accessKey);
        } catch (IOException e) {
            throw new RuntimeException("读取accessKey失败",e);
        }
    }

    // ── Search ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("测试网页搜索")
    public void testSearch(){
        SearchRequest request = SearchRequest.create("DeepSeek V4 模型 发布");
        SearchResponse response = client.post(request).block();

        Assertions.assertNotNull(response, "searchResponse不应该为空");
        Assertions.assertNotNull(response.getResults(), "results 不应该为空（无结果时是空数组）");

        for (SearchResponse.Result result : response.getResults()) {
            // schema 中 required 的字段
            Assertions.assertNotNull(result.getTitle(), "title 不应该为空");
            Assertions.assertNotNull(result.getUrl(), "url 不应该为空");
            Assertions.assertNotNull(result.getSnippet(), "snippet 不应该为空");
            Assertions.assertNotNull(result.getSummary(), "summary 不应该为空");
            Assertions.assertNotNull(result.getContent(), "content 不应该为 null（无正文时是空字符串）");
            Assertions.assertNotNull(result.getRerankScore(), "rerank_score 不应该为空");
            Assertions.assertNotNull(result.getAuthority(), "authority 不应该为空");
            // 取值范围
            Assertions.assertTrue(result.getRerankScore() <= 3, "rerank_score 应 <= 3: " + result.getRerankScore());
            Assertions.assertTrue(result.getAuthority() >= -1 && result.getAuthority() <= 4,
                    "authority 应在 -1~4: " + result.getAuthority());

            System.out.println("[" + result.getRerankScore() + "] " + result.getTitle()
                    + " -> " + result.getUrl()
                    + " (site=" + result.getSiteName() + ", date=" + result.getDate()
                    + ", authority=" + result.getAuthority() + ")");
        }
    }

    @Test
    @DisplayName("测试网页搜索：count 限制条数与 need_content 返回正文")
    public void testSearchCountAndNeedContent(){
        SearchRequest request = SearchRequest.create("DeepSeek 开源 模型");
        request.setCount(3);
        request.setNeedContent(true);

        SearchResponse response = client.post(request).block();

        Assertions.assertNotNull(response, "searchResponse不应该为空");
        Assertions.assertNotNull(response.getResults(), "results 不应该为空");
        Assertions.assertTrue(response.getResults().size() <= 3,
                "返回条数不应超过 count=3，实际 " + response.getResults().size());

        for (SearchResponse.Result result : response.getResults()) {
            // need_content=true 时，有正文的结果 content 不应为 null
            Assertions.assertNotNull(result.getContent(), "need_content=true 时 content 不应该为 null");
            System.out.println(result.getTitle() + " | 正文长度="
                    + (result.getContent() == null ? 0 : result.getContent().length()));
        }
    }

    @Test
    @DisplayName("测试网页搜索：ori_question 与搜索语法")
    public void testSearchOriQuestionAndSyntax(){
        SearchRequest request = SearchRequest.create("DeepSeek site:github.com lang:zh after:2025");
        request.setOriQuestion("DeepSeek 最近发布了什么模型");
        request.setCount(5);

        SearchResponse response = client.post(request).block();

        Assertions.assertNotNull(response, "searchResponse不应该为空");
        Assertions.assertNotNull(response.getResults(), "results 不应该为空");
        Assertions.assertTrue(response.getResults().size() <= 5,
                "返回条数不应超过 count=5，实际 " + response.getResults().size());
    }

    @Test
    @DisplayName("测试网页搜索：count 越界返回 4xx 错误")
    public void testSearchCountOutOfRange(){
        SearchRequest request = SearchRequest.create("DeepSeek");
        request.setCount(100);

        DeepSeekException ex = Assertions.assertThrows(DeepSeekException.class,
                () -> client.post(request).block(), "count 越界应返回错误");
        System.out.println("count 越界的错误信息: " + ex.getMessage());
        Assertions.assertTrue(ex.getMessage() != null && ex.getMessage().contains("count"),
                "错误信息应包含字段名 count，实际: " + ex.getMessage());
    }

    @Test
    @DisplayName("测试网页搜索：空 query 返回 4xx 错误")
    public void testSearchBlankQuery(){
        SearchRequest request = SearchRequest.create(" ");

        Assertions.assertThrows(DeepSeekException.class,
                () -> client.post(request).block(), "全空白的 query 应返回错误");
    }

    // ── Fetch ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("测试网页抓取")
    public void testFetch(){
        FetchRequest request = FetchRequest.create("https://example.com");
        FetchResponse response = client.post(request).block();

        Assertions.assertNotNull(response, "fetchResponse不应该为空");
        Assertions.assertNotNull(response.getTitle(), "title 不应该为 null（缺失时是空字符串）");
        Assertions.assertNotNull(response.getContent(), "content 不应该为空");
        Assertions.assertFalse(response.getContent().isBlank(), "抓取到的正文不应为空白");

        System.out.println("抓取标题: " + response.getTitle());
        System.out.println("正文长度: " + response.getContent().length());
        System.out.println("正文片段: " + response.getContent().substring(0, Math.min(200, response.getContent().length())));
    }

    @Test
    @DisplayName("测试网页抓取：URL 不可用时返回 4xx 错误")
    public void testFetchUnreachableUrl(){
        // .invalid 是保留顶级域，永远不会解析成功
        FetchRequest request = FetchRequest.create("https://not-exists-8f3a1c.invalid/article");

        DeepSeekException ex = Assertions.assertThrows(DeepSeekException.class,
                () -> client.post(request).block(), "不可用 URL 应返回错误");
        System.out.println("不可用 URL 的错误信息: " + ex.getMessage());
    }

    @Test
    @DisplayName("测试网页抓取：非法 URL 返回 4xx 错误")
    public void testFetchIllegalUrl(){
        FetchRequest request = FetchRequest.create("not-a-valid-url");

        Assertions.assertThrows(DeepSeekException.class,
                () -> client.post(request).block(), "非法 URL 应返回错误");
    }
}
