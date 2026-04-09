package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRoleCreateRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserResponse;
import com.aoxiaoyou.admin.dto.response.PermissionResponse;
import com.aoxiaoyou.admin.dto.response.RoleResponse;
import com.aoxiaoyou.admin.entity.Permission;
import com.aoxiaoyou.admin.entity.Role;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.mapper.PermissionMapper;
import com.aoxiaoyou.admin.mapper.RoleMapper;
import com.aoxiaoyou.admin.mapper.SysAdminMapper;
import com.aoxiaoyou.admin.service.AdminRbacService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRbacServiceImpl implements AdminRbacService {

    private final SysAdminMapper sysAdminMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PageResponse<AdminUserResponse> pageAdminUsers(long pageNum, long pageSize, String keyword) {
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<SysAdmin>()
                .and(StringUtils.hasText(keyword), q -> q.like(SysAdmin::getUsername, keyword)
                        .or().like(SysAdmin::getNickname, keyword)
                        .or().like(SysAdmin::getEmail, keyword))
                .orderByDesc(SysAdmin::getUpdatedAt);
        Page<SysAdmin> page = sysAdminMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<AdminUserResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::mapUser).toList());
        return PageResponse.of(result);
    }

    @Override
    public List<RoleResponse> listRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByAsc(Role::getSortOrder).orderByAsc(Role::getId))
                .stream().map(this::mapRole).collect(Collectors.toList());
    }

    @Override
    public RoleResponse createRole(AdminRoleCreateRequest request) {
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setSortOrder(request.getSortOrder() == null ? 99 : request.getSortOrder());
        role.setIsSystem(request.getIsSystem() == null ? 0 : request.getIsSystem());
        role.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "1");
        roleMapper.insert(role);
        return mapRole(roleMapper.selectById(role.getId()));
    }

    @Override
    public List<PermissionResponse> listPermissions(String module, String type) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<Permission>()
                .eq(StringUtils.hasText(module), Permission::getModule, module)
                .eq(StringUtils.hasText(type), Permission::getPermType, type)
                .orderByAsc(Permission::getModule)
                .orderByAsc(Permission::getSortOrder)
                .orderByAsc(Permission::getId);
        return permissionMapper.selectList(wrapper).stream().map(this::mapPermission).toList();
    }

    @Override
    public List<PermissionResponse> getRolePermissions(Long roleId) {
        String sql = "SELECT p.* FROM permissions p INNER JOIN role_permissions rp ON p.id = rp.permission_id WHERE rp.role_id = ? ORDER BY p.module, p.sort_order, p.id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> PermissionResponse.builder()
                .id(rs.getLong("id"))
                .permCode(rs.getString("perm_code"))
                .permName(rs.getString("perm_name"))
                .module(rs.getString("module"))
                .permType(rs.getString("perm_type"))
                .parentId(rs.getLong("parent_id"))
                .path(rs.getString("path"))
                .method(rs.getString("method"))
                .description(rs.getString("description"))
                .sortOrder(rs.getInt("sort_order"))
                .build(), roleId);
    }

    @Override
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }
        permissionIds.forEach(permissionId -> jdbcTemplate.update(
                "INSERT INTO role_permissions (role_id, permission_id, created_at, _openid) VALUES (?, ?, NOW(), '')",
                roleId, permissionId));
    }

    private AdminUserResponse mapUser(SysAdmin item) {
        return AdminUserResponse.builder()
                .id(item.getId())
                .username(item.getUsername())
                .displayName(item.getNickname())
                .email(item.getEmail())
                .phone(item.getPhone())
                .avatarUrl(item.getAvatarUrl())
                .department(item.getStatus() != null && item.getStatus().equals("active") ? "系统管理" : "未分组")
                .isSuperuser("admin".equals(item.getUsername()) ? 1 : 0)
                .status(item.getStatus())
                .lastLoginAt(item.getLastLoginAt())
                .lastLoginIp(item.getLastLoginIp())
                .build();
    }

    private RoleResponse mapRole(Role item) {
        return RoleResponse.builder()
                .id(item.getId())
                .roleCode(item.getRoleCode())
                .roleName(item.getRoleName())
                .description(item.getDescription())
                .isSystem(item.getIsSystem())
                .sortOrder(item.getSortOrder())
                .status(item.getStatus())
                .build();
    }

    private PermissionResponse mapPermission(Permission item) {
        return PermissionResponse.builder()
                .id(item.getId())
                .permCode(item.getPermCode())
                .permName(item.getPermName())
                .module(item.getModule())
                .permType(item.getPermType())
                .parentId(item.getParentId())
                .path(item.getPath())
                .method(item.getMethod())
                .description(item.getDescription())
                .sortOrder(item.getSortOrder())
                .build();
    }
}
