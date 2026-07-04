package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置他人密码 DTO (超级管理员使用)
 */
@Data
public class ResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "管理员ID不能为空")
    private Long adminId;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
