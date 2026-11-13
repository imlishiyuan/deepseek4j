package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ToolTypeEnums {
    FUNCTION("function","function"),
    /** 服务端联网搜索工具：声明后由服务端执行搜索，客户端不执行任何工具 */
    DEEPSEEK_WEB_SEARCH("deepseek_web_search","服务端联网搜索工具"),
    /** 服务端阅读链接工具：声明后服务端自动抓取用户消息中的网页 */
    DEEPSEEK_READ_URL("deepseek_read_url","服务端阅读链接工具"),

    ;
    public final String code;
    public final String desc;

    public static ToolTypeEnums fromCode(String code){
        for (ToolTypeEnums roleEnums : ToolTypeEnums.values()) {
            if (roleEnums.code.equals(code)){
                return roleEnums;
            }
        }
        return null;
    }

}
