package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 普通用户展示 VO (不含密码)
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private Integer gender;

    private String avatar;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createTime;
}
