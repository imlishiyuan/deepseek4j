package cn.lishiyuan.deepseek.response;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class BalanceInfoResponse {
    @JSONField(name = "is_available")
    private Boolean isAvailable;
    @JSONField(name = "balance_infos")
    private List<Info> balanceInfos;

    @Data
    public static class Info {
        private String currency;
        @JSONField(name = "total_balance")
        private String totalBalance;
        @JSONField(name = "granted_balance")
        private String grantedBalance;
        @JSONField(name = "topped_up_balance")
        private String toppedUpBalance;
    }
}
