package cn.lishiyuan.deepseek.tools;

import cn.lishiyuan.deepseek.agent.DefParam;
import cn.lishiyuan.deepseek.agent.DefTool;

import java.util.List;

/**
 * 模拟地图工具（测试用）：景点、美食、路线，返回可预期的假数据。
 */
public class MapTools {

    @DefTool(name = "search_scenic_spots", description = "搜索指定城市的著名景点")
    public List<ScenicSpot> searchScenicSpots(
            @DefParam(name = "city", description = "城市名称，例如：阿勒泰") String city) {
        return List.of(
                new ScenicSpot("喀纳斯景区", "5A", "高山湖泊与原始森林，秋季层林尽染"),
                new ScenicSpot("禾木村", "5A", "图瓦人村落，以晨雾与木屋闻名"),
                new ScenicSpot("可可托海", "5A", "额尔齐斯河大峡谷与神钟山"));
    }

    @DefTool(name = "search_local_food", description = "搜索指定城市的特色美食")
    public List<LocalFood> searchLocalFood(
            @DefParam(name = "city", description = "城市名称，例如：阿勒泰") String city) {
        return List.of(
                new LocalFood("烤全羊", "哈萨克风味，外焦里嫩"),
                new LocalFood("手抓肉", "原味羊肉，佐以洋葱与盐"),
                new LocalFood("奶疙瘩", "哈萨克传统奶制品，酸甜耐嚼"),
                new LocalFood("马肠子", "熏制马肉肠，风味独特"));
    }

    @DefTool(name = "plan_route", description = "规划从起点到终点的路线")
    public Route planRoute(
            @DefParam(name = "request", description = "路线规划条件") RouteRequest request) {
        boolean walking = "walking".equalsIgnoreCase(request.mode());
        return new Route(
                request.from(),
                request.to(),
                request.mode() == null ? "driving" : request.mode(),
                walking ? 3.2 : 96.5,
                walking ? 42 : 128,
                "沿 G217 行驶，注意山区限速与夜间行车");
    }

    /** 路线规划条件。 */
    public record RouteRequest(
            @DefParam(name = "from", description = "起点，例如：阿勒泰市区") String from,
            @DefParam(name = "to", description = "终点，例如：喀纳斯景区") String to,
            @DefParam(name = "mode", description = "出行方式：driving / walking / transit") String mode) {
    }

    /** 景点。 */
    public record ScenicSpot(String name, String level, String description) {
    }

    /** 特色美食。 */
    public record LocalFood(String name, String description) {
    }

    /** 路线。 */
    public record Route(String from, String to, String mode, double distanceKm,
                        int durationMinutes, String tip) {
    }
}
