package cn.lishiyuan.deepseek.agent;

import cn.lishiyuan.deepseek.api.chat.Tool;
import cn.lishiyuan.deepseek.api.chat.ToolCall;

import java.util.List;

public interface ToolsCaller {

    List<Tool> tools();

    String call(ToolCall tool);

}
