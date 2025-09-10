package com.ruoyi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户余额响应DTO
 */
@Data
public class UserBalanceResponse {
    
    private Long userId;
    private String userName;
    private BigDecimal totalPaid;      // 总共支付
    private BigDecimal totalOwed;      // 总共欠款
    private BigDecimal balance;        // 余额 (正数表示别人欠我，负数表示我欠别人)
    private List<BalanceDetail> details;
    
    @Data
    public static class BalanceDetail {
        private String type;            // "owed" 或 "should_receive"
        private String description;
        private BigDecimal amount;
        private String relatedUserName;
    }
}
