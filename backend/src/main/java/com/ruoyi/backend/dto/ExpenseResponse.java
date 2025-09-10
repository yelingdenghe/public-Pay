package com.ruoyi.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用响应DTO
 */
@Data
public class ExpenseResponse {
    
    private Long id;
    private String description;
    private BigDecimal amount;
    private List<PaymentResponse> payments;
    private String categoryName;
    private String splitType;
    private LocalDateTime expenseDate;
    private LocalDateTime createdAt;
    private List<ExpenseShareResponse> shares;
    
    @Data
    public static class PaymentResponse {
        private Long id;
        private String payerName;
        private BigDecimal amount;
    }
    
    @Data
    public static class ExpenseShareResponse {
        private Long id;
        private String userName;
        private BigDecimal amount;
        private BigDecimal customAmount;
        private Boolean isPaid;
    }
}
