package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员展示 VO (不含密码)
 */
@Data
public class AdminVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    private Long roleId;

    private String roleName;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;
}
