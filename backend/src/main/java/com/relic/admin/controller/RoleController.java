package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.relic.admin.common.Result;
import com.relic.admin.dto.RoleDTO;
import com.relic.admin.entity.SysRole;
import com.relic.admin.service.SysRoleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/admin/roles")
public class RoleController {

    private final SysRoleService sysRoleService;

    public RoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 查询全部角色 (用于下拉选择等)
     */
    @GetMapping
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }

    /**
     * 根据ID查询角色详情
     */
    @GetMapping("/{id}")
    public Result<SysRole> get(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    /**
     * 创建角色 (需要 system:role:create 权限)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SaCheckPermission("system:role:create")
    public Result<Void> create(@RequestBody @Valid RoleDTO dto) {
        sysRoleService.createRole(dto);
        return Result.success();
    }

    /**
     * 更新角色 (需要 system:role:update 权限)
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:update")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid RoleDTO dto) {
        dto.setId(id);
        sysRoleService.updateRole(dto);
        return Result.success();
    }

    /**
     * 删除角色 (需要 system:role:delete 权限)
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:delete")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取角色已分配的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> permissions(@PathVariable Long id) {
        return Result.success(sysRoleService.getRolePermissionIds(id));
    }

    /**
     * 为角色分配权限 (需要 system:role:perm 权限)
     */
    @PutMapping("/{id}/permissions")
    @SaCheckPermission("system:role:perm")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        sysRoleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
