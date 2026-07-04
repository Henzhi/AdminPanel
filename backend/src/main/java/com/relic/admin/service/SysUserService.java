package com.relic.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.common.PageResult;
import com.relic.admin.dto.ResetUserPasswordDTO;
import com.relic.admin.dto.UserDTO;
import com.relic.admin.entity.SysUser;
import com.relic.admin.vo.UserVO;

/**
 * 普通用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询普通用户列表
     *
     * @param page     页码
     * @param size     每页条数
     * @param username 用户名(模糊)
     * @param status   状态
     * @return 分页结果
     */
    PageResult<UserVO> pageUserVO(Integer page, Integer size, String username, Integer status);

    /**
     * 根据ID查询用户详情
     */
    UserVO getUserVOById(Long id);

    /**
     * 创建普通用户
     */
    void createUser(UserDTO dto);

    /**
     * 更新普通用户
     */
    void updateUser(UserDTO dto);

    /**
     * 删除普通用户 (逻辑删除)
     */
    void deleteUser(Long id);

    /**
     * 重置普通用户密码
     */
    void resetPassword(ResetUserPasswordDTO dto);

    /**
     * 切换用户启用/禁用状态
     */
    void toggleStatus(Long id);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);
}
