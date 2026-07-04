package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置普通用户密码 DTO (管理员使用)
 */
@Data
public class ResetUserPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
