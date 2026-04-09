package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private Integer sortOrder;

    private Integer isSystem;

    private String status;
}
