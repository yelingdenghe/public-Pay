package com.ruoyi.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 费用分类响应DTO
 */
@Data
public class ExpenseCategoryResponse {
    
    private Long id;
    private String name;
    private String description;
    private Boolean isDefault;
    private LocalDateTime createdAt;
}
