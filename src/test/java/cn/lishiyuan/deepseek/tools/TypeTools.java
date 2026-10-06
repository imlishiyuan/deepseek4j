package cn.lishiyuan.deepseek.tools;

import cn.lishiyuan.deepseek.agent.DefParam;
import cn.lishiyuan.deepseek.agent.DefTool;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 参数类型覆盖工具（测试用）：每个方法回显自己收到的参数，用于验证
 * {@link cn.lishiyuan.deepseek.agent.DefaultToolsCaller} 对各类参数的解析。
 * <p>注意 POJO 用 {@code @Data}（有标准 getter/setter）而不是 record ——
 * 现有 schema 生成基于 BeanUtils 的 PropertyDescriptor，record 的属性识别不到。
 */
public class TypeTools {

    public enum Color {
        RED, GREEN, BLUE
    }

    @Data
    public static class Point {
        @DefParam(name = "x", description = "横坐标")
        private int x;
        @DefParam(name = "y", description = "纵坐标")
        private int y;
    }

    @Data
    public static class Shape {
        @DefParam(name = "name", description = "形状名称")
        private String name;
        @DefParam(name = "center", description = "中心点")
        private Point center;
    }

    /** record 类型的参数：BeanUtils 版 schema 生成认不出它，Jackson 版可以。 */
    public record Geo(double lat, double lng) {
    }

    // ── 标量 ────────────────────────────────────────────────────────────────

    @DefTool(name = "echo_string", description = "回显字符串")
    public String echoString(@DefParam(name = "v", description = "字符串") String v) {
        return v;
    }

    @DefTool(name = "echo_char", description = "回显字符")
    public char echoChar(@DefParam(name = "v", description = "单个字符") char v) {
        return v;
    }

    @DefTool(name = "echo_int", description = "回显基本类型 int")
    public int echoInt(@DefParam(name = "v", description = "int") int v) {
        return v;
    }

    @DefTool(name = "echo_integer", description = "回显包装类型 Integer")
    public Integer echoInteger(@DefParam(name = "v", description = "Integer") Integer v) {
        return v;
    }

    @DefTool(name = "echo_long", description = "回显 long")
    public long echoLong(@DefParam(name = "v", description = "long") long v) {
        return v;
    }

    @DefTool(name = "echo_double", description = "回显 double")
    public double echoDouble(@DefParam(name = "v", description = "double") double v) {
        return v;
    }

    @DefTool(name = "echo_float", description = "回显 float")
    public float echoFloat(@DefParam(name = "v", description = "float") float v) {
        return v;
    }

    @DefTool(name = "echo_boolean", description = "回显 boolean")
    public boolean echoBoolean(@DefParam(name = "v", description = "boolean") boolean v) {
        return v;
    }

    @DefTool(name = "echo_bigdecimal", description = "回显 BigDecimal")
    public BigDecimal echoBigDecimal(@DefParam(name = "v", description = "BigDecimal") BigDecimal v) {
        return v;
    }

    @DefTool(name = "echo_biginteger", description = "回显 BigInteger")
    public BigInteger echoBigInteger(@DefParam(name = "v", description = "BigInteger") BigInteger v) {
        return v;
    }

    @DefTool(name = "echo_enum", description = "回显枚举")
    public Color echoEnum(@DefParam(name = "v", description = "RED/GREEN/BLUE") Color v) {
        return v;
    }

    // ── 数组 / 集合 / Map ───────────────────────────────────────────────────

    @DefTool(name = "echo_int_array", description = "回显 int[]")
    public int[] echoIntArray(@DefParam(name = "v", description = "整数数组") int[] v) {
        return v;
    }

    @DefTool(name = "echo_string_array", description = "回显 String[]")
    public String[] echoStringArray(@DefParam(name = "v", description = "字符串数组") String[] v) {
        return v;
    }

    @DefTool(name = "echo_string_list", description = "回显 List<String>")
    public List<String> echoStringList(@DefParam(name = "v", description = "字符串列表") List<String> v) {
        return v;
    }

    @DefTool(name = "echo_pojo_list", description = "回显 List<Point>")
    public List<Point> echoPojoList(@DefParam(name = "v", description = "Point 列表") List<Point> v) {
        return v;
    }

    @DefTool(name = "echo_set", description = "回显 Set<String>")
    public Set<String> echoSet(@DefParam(name = "v", description = "字符串集合") Set<String> v) {
        return v;
    }

    @DefTool(name = "echo_map", description = "回显 Map<String,Integer>")
    public Map<String, Integer> echoMap(@DefParam(name = "v", description = "键值对") Map<String, Integer> v) {
        return v;
    }

    // ── POJO / 嵌套 / 时间 ──────────────────────────────────────────────────

    @DefTool(name = "echo_pojo", description = "回显 Point")
    public Point echoPojo(@DefParam(name = "v", description = "坐标点") Point v) {
        return v;
    }

    @DefTool(name = "echo_nested_pojo", description = "回显嵌套 POJO Shape")
    public Shape echoNestedPojo(@DefParam(name = "v", description = "形状") Shape v) {
        return v;
    }

    @DefTool(name = "echo_record", description = "回显 record Geo")
    public Geo echoRecord(@DefParam(name = "v", description = "经纬度") Geo v) {
        return v;
    }

    @DefTool(name = "echo_date", description = "回显 java.util.Date")
    public Date echoDate(@DefParam(name = "v", description = "时间戳（毫秒）或 ISO-8601") Date v) {
        return v;
    }

    // ── 边界 ────────────────────────────────────────────────────────────────

    @DefTool(name = "echo_two", description = "回显两个字符串，用于验证缺参")
    public List<String> echoTwo(@DefParam(name = "a", description = "第一个") String a,
                                @DefParam(name = "b", description = "第二个") String b) {
        return List.of(String.valueOf(a), String.valueOf(b));
    }

    @DefTool(name = "no_arg", description = "无参数工具")
    public String noArg() {
        return "ok";
    }
}
