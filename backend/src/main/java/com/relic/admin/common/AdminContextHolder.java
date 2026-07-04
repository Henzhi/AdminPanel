package com.relic.admin.common;

import cn.dev33.satoken.stp.StpUtil;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.service.SysAdminService;
import org.springframework.stereotype.Component;

/**
 * 管理员上下文工具 - 获取当前登录管理员信息
 */
@Component
public class AdminContextHolder {

    private static SysAdminService sysAdminService;

    public AdminContextHolder(SysAdminService sysAdminService) {
        AdminContextHolder.sysAdminService = sysAdminService;
    }

    /**
     * 获取当前登录管理员ID
     */
    public static Long getCurrentAdminId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录管理员用户名
     */
    public static String getCurrentAdminName() {
        Long adminId = getCurrentAdminId();
        if (adminId == null) {
            return "unknown";
        }
        SysAdmin admin = sysAdminService.getById(adminId);
        return admin != null ? admin.getUsername() : "unknown";
    }

    /**
     * 检查是否登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 检查权限
     */
    public static boolean hasPermission(String permission) {
        return StpUtil.hasPermission(permission);
    }

    /**
     * 检查角色
     */
    public static boolean hasRole(String role) {
        return StpUtil.hasRole(role);
    }
}
