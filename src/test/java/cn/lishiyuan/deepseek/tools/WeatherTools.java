package cn.lishiyuan.deepseek.tools;

import cn.lishiyuan.deepseek.agent.DefParam;
import cn.lishiyuan.deepseek.agent.DefTool;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟天气预报工具（测试用）：返回可预期的假数据，不访问网络。
 */
public class WeatherTools {

    @DefTool(name = "get_weather", description = "查询指定城市当前的天气情况")
    public Weather getWeather(
            @DefParam(name = "city", description = "城市名称，例如：阿勒泰") String city) {
        return new Weather(city, "晴", 18, 35, "西北风 3 级", "天气不错，适合出行");
    }

    @DefTool(name = "get_weather_forecast", description = "查询指定城市未来几天的天气预报")
    public List<DailyForecast> getWeatherForecast(
            @DefParam(name = "city", description = "城市名称，例如：阿勒泰") String city,
            @DefParam(name = "days", description = "查询天数，取值 1-7") Integer days) {
        int size = days == null ? 3 : Math.max(1, Math.min(days, 7));
        List<DailyForecast> forecasts = new ArrayList<>(size);
        for (int day = 1; day <= size; day++) {
            boolean rainy = day % 3 == 0;
            forecasts.add(new DailyForecast(
                    "第 " + day + " 天",
                    rainy ? "阵雨" : "晴",
                    10 + day,
                    20 + day,
                    rainy ? "有阵雨，注意保暖" : "适合出行"));
        }
        return forecasts;
    }

    /** 当前天气。 */
    public record Weather(String city, String condition, int temperatureC, int humidity,
                          String wind, String suggestion) {
    }

    /** 单日预报。 */
    public record DailyForecast(String day, String condition, int lowC, int highC, String suggestion) {
    }
}
