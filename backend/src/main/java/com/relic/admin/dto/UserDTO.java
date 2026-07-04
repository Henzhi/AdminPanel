package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 普通用户创建/更新 DTO
 *
 * <p>创建时 id 为空，更新时由控制器将路径参数 id 写入。</p>
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，更新时使用
     */
    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 明文密码。创建时必填，更新时为空表示不修改密码。
     */
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private Integer gender;
}
