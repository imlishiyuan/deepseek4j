package cn.lishiyuan.deepseek.api.platform;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ListModelResponse extends BaseResponse {
    @JsonProperty("object")
    private String object;
    @JsonProperty("data")
    private List<Model> data;


    @Data
    public static class Model {

        @JsonProperty("id")
        private String id;
        @JsonProperty("object")
        private String object;
        @JsonProperty("owned_by")
        private String ownedBy;
    }

}
