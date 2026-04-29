package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRoleCreateRequest;
import com.aoxiaoyou.admin.dto.request.AdminUserUpdateRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserResponse;
import com.aoxiaoyou.admin.dto.response.PermissionResponse;
import com.aoxiaoyou.admin.dto.response.RoleResponse;
import com.aoxiaoyou.admin.service.AdminRbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/system")
@RequiredArgsConstructor
public class AdminRbacController {

    private final AdminRbacService rbacService;

    @GetMapping("/admin-users")
    public ApiResponse<PageResponse<AdminUserResponse>> pageAdminUsers(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(rbacService.pageAdminUsers(pageNum, pageSize, keyword));
    }

    @PutMapping("/admin-users/{adminId}")
    public ApiResponse<AdminUserResponse> updateAdminUser(@PathVariable Long adminId, @RequestBody AdminUserUpdateRequest request) {
        return ApiResponse.success(rbacService.updateAdminUser(adminId, request));
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> listRoles() {
        return ApiResponse.success(rbacService.listRoles());
    }

    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@RequestBody AdminRoleCreateRequest request) {
        return ApiResponse.success(rbacService.createRole(request));
    }

    @GetMapping("/permissions")
    public ApiResponse<List<PermissionResponse>> listPermissions(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type) {
        return ApiResponse.success(rbacService.listPermissions(module, type));
    }

    @GetMapping("/roles/{roleId}/permissions")
    public ApiResponse<List<PermissionResponse>> getRolePermissions(@PathVariable Long roleId) {
        return ApiResponse.success(rbacService.getRolePermissions(roleId));
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ApiResponse<Void> updateRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        rbacService.updateRolePermissions(roleId, permissionIds);
        return ApiResponse.success(null);
    }
}

