package com.relic.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.entity.SysPermission;
import com.relic.admin.vo.MenuVO;
import com.relic.admin.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 查询全部权限 (按 sort_order 排序)
     */
    List<SysPermission> getAllPermissions();

    /**
     * 获取权限树 (包含菜单与按钮)
     */
    List<PermissionVO> getPermissionTree();

    /**
     * 根据角色ID查询其拥有的权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Long roleId);

    /**
     * 根据管理员ID获取其动态菜单树 (仅 perm_type=1 的菜单)
     */
    List<MenuVO> getMenuTreeByAdminId(Long adminId);
}
