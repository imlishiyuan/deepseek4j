package cn.lishiyuan.deepseek.api.platform;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class BalanceInfoResponse extends BaseResponse {
    @JsonProperty("is_available")
    private Boolean isAvailable;
    @JsonProperty("balance_infos")
    private List<Info> balanceInfos;

    @Data
    public static class Info {
        private String currency;
        @JsonProperty("total_balance")
        private String totalBalance;
        @JsonProperty("granted_balance")
        private String grantedBalance;
        @JsonProperty("topped_up_balance")
        private String toppedUpBalance;
    }
}
