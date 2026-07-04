package com.relic.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.dto.RoleDTO;
import com.relic.admin.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表
     */
    IPage<SysRole> pageRoles(Page<SysRole> page, String roleName);

    /**
     * 创建角色 (同时分配权限)
     */
    void createRole(RoleDTO dto);

    /**
     * 更新角色 (同时重新分配权限)
     */
    void updateRole(RoleDTO dto);

    /**
     * 删除角色 (同时清理角色-权限关联)
     */
    void deleteRole(Long id);

    /**
     * 为角色分配权限 (先清空后插入)
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色已分配的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
}
