package com.ruoyi.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用添加请求DTO
 */
@Data
public class ExpenseRequest {
    
    @NotBlank(message = "费用描述不能为空")
    private String description;
    
    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额必须大于0")
    private BigDecimal amount;
    
    // 支持多付款人
    @NotEmpty(message = "付款人列表不能为空")
    private List<PaymentRequest> payments;
    
    private Long categoryId;
    
    @NotNull(message = "分摊类型不能为空")
    private String splitType;
    
    private LocalDateTime expenseDate;
    
    @NotEmpty(message = "分摊用户列表不能为空")
    private List<Long> shareUserIds;
    
    private List<CustomShareRequest> customShares;
    
    @Data
    public static class PaymentRequest {
        @NotNull(message = "付款人ID不能为空")
        private Long payerId;
        
        @NotNull(message = "付款金额不能为空")
        @DecimalMin(value = "0.01", message = "付款金额必须大于0")
        private BigDecimal amount;
    }
    
    @Data
    public static class CustomShareRequest {
        private Long userId;
        
        @DecimalMin(value = "0.00", message = "分摊金额不能为负数")
        private BigDecimal amount;
    }
}
