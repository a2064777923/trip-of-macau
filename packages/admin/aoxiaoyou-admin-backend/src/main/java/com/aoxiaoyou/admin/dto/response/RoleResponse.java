package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer isSystem;
    private Integer sortOrder;
    private String status;
}
