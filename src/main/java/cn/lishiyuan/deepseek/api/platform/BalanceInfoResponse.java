package cn.lishiyuan.deepseek.api.platform;

import cn.lishiyuan.deepseek.api.BaseResponse;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class BalanceInfoResponse extends BaseResponse {
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
