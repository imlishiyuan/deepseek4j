package cn.lishiyuan.deepseek.tools;

import cn.lishiyuan.deepseek.agent.DefParam;
import cn.lishiyuan.deepseek.agent.DefTool;

import java.util.List;

/**
 * 模拟机票工具（测试用）：航班查询与预订，返回可预期的假数据。
 * <p>参数用 record 承载（对应现实里的「请求对象」），属性说明写在 record 组件上。
 */
public class TripTools {

    @DefTool(name = "search_flights", description = "查询指定日期从出发城市到到达城市的航班")
    public List<Flight> searchFlights(
            @DefParam(name = "query", description = "航班查询条件") FlightQuery query) {
        return List.of(
                new Flight("CZ6886", query.from(), query.to(), query.date(), "08:20", "13:45", 1680.0, 12),
                new Flight("MU2882", query.from(), query.to(), query.date(), "14:10", "19:30", 1520.0, 5),
                new Flight("HU7605", query.from(), query.to(), query.date(), "20:05", "01:20", 1299.0, 21));
    }

    @DefTool(name = "book_flight", description = "为指定乘客预订航班，返回订单结果")
    public Booking bookFlight(
            @DefParam(name = "request", description = "预订信息") BookingRequest request) {
        String seed = String.valueOf(request.passenger()) + request.flightNo() + request.date();
        return new Booking(true,
                "ORD" + Integer.toUnsignedString(seed.hashCode()),
                request.passenger(), request.flightNo(), request.date(),
                "已出票，请提前 90 分钟到达机场办理值机");
    }

    /** 航班查询条件。 */
    public record FlightQuery(
            @DefParam(name = "from", description = "出发城市，例如：西安") String from,
            @DefParam(name = "to", description = "到达城市，例如：阿勒泰") String to,
            @DefParam(name = "date", description = "出发日期，格式 yyyy-MM-dd") String date) {
    }

    /** 预订信息。 */
    public record BookingRequest(
            @DefParam(name = "passenger", description = "乘客姓名，例如：李干嘛") String passenger,
            @DefParam(name = "flightNo", description = "航班号，例如：CZ6886") String flightNo,
            @DefParam(name = "date", description = "出发日期，格式 yyyy-MM-dd") String date) {
    }

    /** 航班。 */
    public record Flight(String flightNo, String from, String to, String date,
                         String departTime, String arriveTime, double price, int remainingSeats) {
    }

    /** 订单。 */
    public record Booking(boolean success, String orderId, String passenger, String flightNo,
                          String date, String message) {
    }
}
