package cn.lishiyuan.deepseek.api.platform;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class ListModelResponse extends BaseResponse {
    @JSONField(name="object")
    private String object;
    @JSONField(name="data")
    private List<Model> data;


    @Data
    public static class Model {

        @JSONField(name="id")
        private String id;
        @JSONField(name="object")
        private String object;
        @JSONField(name = "owned_by")
        private String ownedBy;
    }

}
