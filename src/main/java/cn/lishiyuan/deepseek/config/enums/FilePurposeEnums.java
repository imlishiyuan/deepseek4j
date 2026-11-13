package cn.lishiyuan.deepseek.config.enums;

import lombok.AllArgsConstructor;

/**
 * 文件用途 / 文档解析方式（multipart 表单字段 {@code purpose}）。
 *
 * <p>Files API 上传文件与 OCR 接口共用：
 * <ul>
 *   <li>{@code user_data}：普通文件，不做文档解析（仅 Files API）；</li>
 *   <li>{@code text_recognition}：只提取文字，文档里的图表、照片、示意图会被丢弃；</li>
 *   <li>{@code rich_layout_recognition}：提取文字，并把文档里的图裁出来按原位置与文字一起交给模型。</li>
 * </ul>
 * OCR 接口只支持后两种。
 *
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/document_parsing">文件解析</a>
 * @see <a href="https://api-docs.deepseek.com/zh-cn/guides/ocr">OCR</a>
 */
@AllArgsConstructor
public enum FilePurposeEnums {
    /** 普通文件，不做文档解析（仅 Files API） */
    USER_DATA("user_data", "普通文件"),
    /** 只提取文字 */
    TEXT_RECOGNITION("text_recognition", "只提取文字"),
    /** 文字 + 图片版式 */
    RICH_LAYOUT_RECOGNITION("rich_layout_recognition", "文字 + 图片版式"),
    ;
    public final String code;
    public final String desc;

    public static FilePurposeEnums fromCode(String code) {
        for (FilePurposeEnums anEnum : FilePurposeEnums.values()) {
            if (anEnum.code.equals(code)) {
                return anEnum;
            }
        }
        return null;
    }

}
