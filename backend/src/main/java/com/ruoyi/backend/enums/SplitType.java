package com.ruoyi.backend.enums;

/**
 * 分摊类型枚举
 */
public enum SplitType {
    EQUAL("平均分摊"),
    CUSTOM("自定义分摊");
    
    private final String description;
    
    SplitType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
