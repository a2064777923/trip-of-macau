package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@TableName("test_accounts")
public class TestAccount {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String testGroup;
    
    private Double mockLatitude;
    
    private Double mockLongitude;
    
    private Boolean mockEnabled;
    
    private Long mockPoiId;
    
    private String notes;

    @TableField("_openid")
    private String openid;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}

