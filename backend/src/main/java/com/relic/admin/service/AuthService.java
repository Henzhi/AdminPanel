package com.relic.admin.service;

import com.relic.admin.dto.LoginDTO;
import com.relic.admin.dto.PasswordDTO;
import com.relic.admin.vo.AdminVO;
import com.relic.admin.vo.LoginVO;
import com.relic.admin.vo.MenuVO;

import java.util.List;

/**
 * 认证授权服务接口
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 登出
     */
    void logout();

    /**
     * 获取当前登录管理员信息
     */
    AdminVO getCurrentAdminInfo();

    /**
     * 修改当前管理员自身密码
     */
    void changePassword(PasswordDTO dto);

    /**
     * 获取当前管理员的动态菜单树
     */
    List<MenuVO> getMenuTree();
}
