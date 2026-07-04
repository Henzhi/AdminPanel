package com.relic.admin.controller;

import com.relic.admin.common.Result;
import com.relic.admin.service.SysPermissionService;
import com.relic.admin.vo.PermissionVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/admin/permissions")
public class PermissionController {

    private final SysPermissionService sysPermissionService;

    public PermissionController(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 获取全部权限 (树形结构)
     */
    @GetMapping
    public Result<List<PermissionVO>> list() {
        return Result.success(sysPermissionService.getPermissionTree());
    }

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    public Result<List<PermissionVO>> tree() {
        return Result.success(sysPermissionService.getPermissionTree());
    }
}
