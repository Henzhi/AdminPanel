package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改自身密码 DTO
 */
@Data
public class PasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
