package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录成功返回 VO
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;

    private Long adminId;

    private String username;

    private String realName;

    private String avatar;

    /**
     * 当前管理员拥有的角色编码列表
     */
    private List<String> roles;

    /**
     * 当前管理员拥有的权限编码列表
     */
    private List<String> permissions;
}
