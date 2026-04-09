package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private Long id;
    private String permCode;
    private String permName;
    private String module;
    private String permType;
    private Long parentId;
    private String path;
    private String method;
    private String description;
    private Integer sortOrder;
}
