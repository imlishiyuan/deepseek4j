package cn.lishiyuan.deepseek.agent;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 提供基础数学工具，默认两位小数精度、四舍五入。避免大模型自己运算错误。
 *
 * <p>覆盖：四则运算、幂与开方、三角函数与反三角函数、绝对值 / 对数 / 指数 / 取余。
 * 每个工具都有 {@code scale} 参数控制结果保留的小数位数（默认 2，范围 0~10）。
 * 无法计算时返回「计算失败：原因」字符串而不是抛异常，便于模型读懂并纠正入参。
 */
public class Calculator {

    /** 默认结果小数位数 */
    private static final int DEFAULT_SCALE = 2;

    /** 允许的最大结果小数位数（避免输出过长） */
    private static final int MAX_SCALE = 10;

    /** 角度单位：度 */
    private static final String DEG = "deg";

    /** 角度单位：弧度 */
    private static final String RAD = "rad";

    /** scale 参数的统一描述，保证各工具口径一致 */
    private static final String SCALE_DESC = "结果保留的小数位数，默认 2，范围 0~10";

    // ── 四则运算 ────────────────────────────────────────────────────────────

    @DefTool(name = "calculator_plus", description = "加法：计算 left + right，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String plus(
            @DefParam(name = "left", description = "左操作数，十进制数字字符串，例如 1234.567") String left,
            @DefParam(name = "right", description = "右操作数，十进制数字字符串，例如 890.123") String right,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(parse(left).add(parse(right)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_minus", description = "减法：计算 left - right，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String minus(
            @DefParam(name = "left", description = "被减数，十进制数字字符串") String left,
            @DefParam(name = "right", description = "减数，十进制数字字符串") String right,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(parse(left).subtract(parse(right)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_multiply", description = "乘法：计算 left * right，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String multiply(
            @DefParam(name = "left", description = "乘数，十进制数字字符串") String left,
            @DefParam(name = "right", description = "乘数，十进制数字字符串") String right,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(parse(left).multiply(parse(right)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_divide", description = "除法：计算 left / right，结果保留 scale 位小数（四舍五入，默认 2 位）；除数为 0 时返回错误说明。")
    public String divide(
            @DefParam(name = "left", description = "被除数，十进制数字字符串") String left,
            @DefParam(name = "right", description = "除数，十进制数字字符串，不能为 0") String right,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            int s = resolveScale(scale);
            BigDecimal divisor = parse(right);
            if (divisor.signum() == 0) {
                return "计算失败：除数不能为 0";
            }
            // 除法必须显式给精度，否则无限小数会抛 ArithmeticException
            return parse(left).divide(divisor, s, RoundingMode.HALF_UP).toPlainString();
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    // ── 幂与开方 ────────────────────────────────────────────────────────────

    @DefTool(name = "calculator_square", description = "平方：计算 x 的 2 次方，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String square(
            @DefParam(name = "x", description = "底数，十进制数字字符串") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(parse(x).multiply(parse(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_sqrt", description = "平方根：计算 x 的平方根，结果保留 scale 位小数（四舍五入，默认 2 位）；x 为负数时返回错误说明。")
    public String sqrt(
            @DefParam(name = "x", description = "被开方数，十进制数字字符串，可为 0，不能为负数") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.sqrt(toDouble(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_cbrt", description = "立方根：计算 x 的立方根，结果保留 scale 位小数（四舍五入，默认 2 位）；支持负数。")
    public String cbrt(
            @DefParam(name = "x", description = "被开方数，十进制数字字符串，可为负数") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.cbrt(toDouble(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_power", description = "幂运算：计算 base 的 exponent 次方，结果保留 scale 位小数（四舍五入，默认 2 位）；结果为非实数时返回错误说明。")
    public String power(
            @DefParam(name = "base", description = "底数，十进制数字字符串") String base,
            @DefParam(name = "exponent", description = "指数，十进制数字字符串，可为小数（如 0.5 表示开平方）") String exponent,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.pow(toDouble(base), toDouble(exponent)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    // ── 三角函数 ────────────────────────────────────────────────────────────

    @DefTool(name = "calculator_sin", description = "正弦：计算角 x 的正弦值，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定 x 的角度单位：deg（度，默认）或 rad（弧度）。")
    public String sin(
            @DefParam(name = "x", description = "角度值，十进制数字字符串") String x,
            @DefParam(name = "unit", description = "角度单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.sin(toRadians(toDouble(x), unit)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_cos", description = "余弦：计算角 x 的余弦值，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定 x 的角度单位：deg（度，默认）或 rad（弧度）。")
    public String cos(
            @DefParam(name = "x", description = "角度值，十进制数字字符串") String x,
            @DefParam(name = "unit", description = "角度单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.cos(toRadians(toDouble(x), unit)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_tan", description = "正切：计算角 x 的正切值，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定 x 的角度单位：deg（度，默认）或 rad（弧度）。")
    public String tan(
            @DefParam(name = "x", description = "角度值，十进制数字字符串") String x,
            @DefParam(name = "unit", description = "角度单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.tan(toRadians(toDouble(x), unit)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_asin", description = "反正弦：由正弦值 x 求角度，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定返回角度的单位：deg（度，默认）或 rad（弧度）。x 必须在 [-1,1]。")
    public String asin(
            @DefParam(name = "x", description = "正弦值，十进制数字字符串，范围 [-1,1]") String x,
            @DefParam(name = "unit", description = "返回角度的单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(fromRadians(Math.asin(toDouble(x)), unit), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_acos", description = "反余弦：由余弦值 x 求角度，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定返回角度的单位：deg（度，默认）或 rad（弧度）。x 必须在 [-1,1]。")
    public String acos(
            @DefParam(name = "x", description = "余弦值，十进制数字字符串，范围 [-1,1]") String x,
            @DefParam(name = "unit", description = "返回角度的单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(fromRadians(Math.acos(toDouble(x)), unit), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_atan", description = "反正切：由正切值 x 求角度，结果保留 scale 位小数（四舍五入，默认 2 位）。unit 指定返回角度的单位：deg（度，默认）或 rad（弧度）。")
    public String atan(
            @DefParam(name = "x", description = "正切值，十进制数字字符串") String x,
            @DefParam(name = "unit", description = "返回角度的单位：deg（度，默认）或 rad（弧度）") String unit,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(fromRadians(Math.atan(toDouble(x)), unit), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    // ── 其他常用运算 ────────────────────────────────────────────────────────

    @DefTool(name = "calculator_abs", description = "绝对值：计算 x 的绝对值，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String abs(
            @DefParam(name = "x", description = "十进制数字字符串") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(parse(x).abs(), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_ln", description = "自然对数：计算以 e 为底 x 的对数，结果保留 scale 位小数（四舍五入，默认 2 位）；x 必须大于 0。")
    public String ln(
            @DefParam(name = "x", description = "真数，十进制数字字符串，必须大于 0") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.log(toDouble(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_log10", description = "常用对数：计算以 10 为底 x 的对数，结果保留 scale 位小数（四舍五入，默认 2 位）；x 必须大于 0。")
    public String log10(
            @DefParam(name = "x", description = "真数，十进制数字字符串，必须大于 0") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.log10(toDouble(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_exp", description = "指数：计算 e 的 x 次方，结果保留 scale 位小数（四舍五入，默认 2 位）。")
    public String exp(
            @DefParam(name = "x", description = "指数，十进制数字字符串") String x,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            return format(Math.exp(toDouble(x)), resolveScale(scale));
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    @DefTool(name = "calculator_mod", description = "取余：计算 left 除以 right 的余数（余数符号与被除数一致），结果保留 scale 位小数（四舍五入，默认 2 位）；right 不能为 0。")
    public String mod(
            @DefParam(name = "left", description = "被除数，十进制数字字符串") String left,
            @DefParam(name = "right", description = "除数，十进制数字字符串，不能为 0") String right,
            @DefParam(name = "scale", description = SCALE_DESC) Integer scale) {
        try {
            int s = resolveScale(scale);
            BigDecimal divisor = parse(right);
            if (divisor.signum() == 0) {
                return "计算失败：除数不能为 0";
            }
            return format(parse(left).remainder(divisor), s);
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    // ── 内部辅助 ────────────────────────────────────────────────────────────

    /** 解析操作数：允许前后空白，空值或非数字会抛异常由调用方转成错误说明。 */
    private static BigDecimal parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("操作数不能为空");
        }
        return new BigDecimal(value.trim());
    }

    private static double toDouble(String value) {
        return parse(value).doubleValue();
    }

    /** 解析精度：不传按默认 2 位；超出 0~10 视为非法入参。 */
    private static int resolveScale(Integer scale) {
        if (scale == null) {
            return DEFAULT_SCALE;
        }
        if (scale < 0 || scale > MAX_SCALE) {
            throw new IllegalArgumentException("精度 scale 必须在 0~" + MAX_SCALE + " 之间");
        }
        return scale;
    }

    /** 把角度值按单位换算成弧度；unit 为空或无法识别时按「度」处理。 */
    private static double toRadians(double value, String unit) {
        return RAD.equalsIgnoreCase(trimmed(unit)) ? value : Math.toRadians(value);
    }

    /** 把弧度换算成指定单位的角度；unit 为空或无法识别时按「度」返回。 */
    private static double fromRadians(double value, String unit) {
        return RAD.equalsIgnoreCase(trimmed(unit)) ? value : Math.toDegrees(value);
    }

    private static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    /** 按指定小数位四舍五入，并用普通计数法输出（避免科学计数法，如 1E+3）。 */
    private static String format(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    /** double 结果：先拦掉 NaN / 无穷，再按指定小数位四舍五入。 */
    private static String format(double value, int scale) {
        if (Double.isNaN(value)) {
            return "计算失败：结果不是实数（例如对负数开平方、反三角函数入参超出定义域）";
        }
        if (Double.isInfinite(value)) {
            return "计算失败：结果超出可表示范围（例如对 0 取对数）";
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }

    /** 把异常转成字符串返回，让模型能读懂失败原因，而不是整个工具调用抛异常。 */
    private static String error(RuntimeException e) {
        return "计算失败：" + e.getMessage();
    }
}
