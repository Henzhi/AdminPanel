package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relic.admin.annotation.OperLog;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.dto.AdminDTO;
import com.relic.admin.dto.ResetPasswordDTO;
import com.relic.admin.service.SysAdminService;
import com.relic.admin.vo.AdminVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员管理控制器
 */
@RestController
@RequestMapping("/api/admin/admins")
public class AdminController {

    private final SysAdminService sysAdminService;

    public AdminController(SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    /**
     * 分页查询管理员列表
     */
    @GetMapping
    public Result<PageResult<AdminVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long roleId) {
        Page<AdminVO> p = new Page<>(page, size);
        var result = sysAdminService.pageAdminVO(p, username, roleId);
        PageResult<AdminVO> pageResult = new PageResult<>(result.getTotal(), result.getRecords());
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询管理员详情
     */
    @GetMapping("/{id}")
    public Result<AdminVO> get(@PathVariable Long id) {
        return Result.success(sysAdminService.getAdminVOById(id));
    }

    /**
     * 创建管理员 (需要 system:admin:create 权限)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SaCheckPermission("system:admin:create")
    @OperLog(operationType = Constants.OP_CREATE, operationTarget = "admin")
    public Result<Void> create(@RequestBody @Valid AdminDTO dto) {
        sysAdminService.createAdmin(dto);
        return Result.success();
    }

    /**
     * 更新管理员 (需要 system:admin:update 权限)
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:admin:update")
    @OperLog(operationType = Constants.OP_UPDATE, operationTarget = "admin")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid AdminDTO dto) {
        dto.setId(id);
        sysAdminService.updateAdmin(dto);
        return Result.success();
    }

    /**
     * 删除管理员 (需要 system:admin:delete 权限)
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:admin:delete")
    @OperLog(operationType = Constants.OP_DELETE, operationTarget = "admin")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sysAdminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 重置管理员密码 (需要 system:admin:reset 权限)
     */
    @PutMapping("/{id}/password")
    @SaCheckPermission("system:admin:reset")
    @OperLog(operationType = Constants.OP_PASSWORD_RESET, operationTarget = "admin")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetPasswordDTO dto) {
        dto.setAdminId(id);
        sysAdminService.resetPassword(dto);
        return Result.success();
    }

    /**
     * 切换管理员启用/禁用状态 (需要 system:admin:update 权限)
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:admin:update")
    @OperLog(operationType = Constants.OP_UPDATE, operationTarget = "admin")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        sysAdminService.toggleStatus(id);
        return Result.success();
    }
}
