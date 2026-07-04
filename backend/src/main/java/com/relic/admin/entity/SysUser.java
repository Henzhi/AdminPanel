package com.relic.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 前台普通用户实体 - 对应 sys_user 表
 *
 * <p>与 sys_admin 分表存储，普通用户无角色概念，仅做账号启用/禁用管理。</p>
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    /** 昵称 */
    private String nickname;

    private String email;

    private String phone;

    /** 性别: 0=未知, 1=男, 2=女 */
    private Integer gender;

    private String avatar;

    /** 状态: 1=正常, 0=禁用 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
