package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.relic.admin.annotation.OperLog;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.dto.ResetUserPasswordDTO;
import com.relic.admin.dto.UserDTO;
import com.relic.admin.service.SysUserService;
import com.relic.admin.vo.UserVO;
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
 * 普通用户管理控制器
 *
 * <p>管理前台注册用户账号，与 sys_admin 分表存储。所有写操作均通过 {@link OperLog}
 * 注解接入操作日志审计。</p>
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final SysUserService sysUserService;

    public UserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 分页查询普通用户列表
     */
    @GetMapping
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        PageResult<UserVO> pageResult = sysUserService.pageUserVO(page, size, username, status);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.success(sysUserService.getUserVOById(id));
    }

    /**
     * 创建普通用户 (需要 system:user:create 权限)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SaCheckPermission("system:user:create")
    @OperLog(operationType = Constants.OP_CREATE, operationTarget = "user")
    public Result<Void> create(@RequestBody @Valid UserDTO dto) {
        sysUserService.createUser(dto);
        return Result.success();
    }

    /**
     * 更新普通用户 (需要 system:user:update 权限)
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:update")
    @OperLog(operationType = Constants.OP_UPDATE, operationTarget = "user")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid UserDTO dto) {
        dto.setId(id);
        sysUserService.updateUser(dto);
        return Result.success();
    }

    /**
     * 删除普通用户 (需要 system:user:delete 权限)
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:delete")
    @OperLog(operationType = Constants.OP_DELETE, operationTarget = "user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 重置用户密码 (需要 system:user:reset 权限)
     */
    @PutMapping("/{id}/password")
    @SaCheckPermission("system:user:reset")
    @OperLog(operationType = Constants.OP_PASSWORD_RESET, operationTarget = "user")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody @Valid ResetUserPasswordDTO dto) {
        dto.setUserId(id);
        sysUserService.resetPassword(dto);
        return Result.success();
    }

    /**
     * 切换用户启用/禁用状态 (需要 system:user:update 权限)
     */
    @PutMapping("/{id}/status")
    @SaCheckPermission("system:user:update")
    @OperLog(operationType = Constants.OP_UPDATE, operationTarget = "user")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        sysUserService.toggleStatus(id);
        return Result.success();
    }
}
