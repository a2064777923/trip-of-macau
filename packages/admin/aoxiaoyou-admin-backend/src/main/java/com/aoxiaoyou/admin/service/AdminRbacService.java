package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.dto.request.AdminRoleCreateRequest;
import com.aoxiaoyou.admin.dto.response.RoleResponse;
import com.aoxiaoyou.admin.dto.response.PermissionResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;

import java.util.List;

public interface AdminRbacService {
    PageResponse<AdminUserResponse> pageAdminUsers(long pageNum, long pageSize, String keyword);
    List<RoleResponse> listRoles();
    RoleResponse createRole(AdminRoleCreateRequest request);
    List<PermissionResponse> listPermissions(String module, String type);
    List<PermissionResponse> getRolePermissions(Long roleId);
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
}

