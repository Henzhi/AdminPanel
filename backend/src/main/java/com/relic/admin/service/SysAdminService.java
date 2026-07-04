package com.relic.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.dto.AdminDTO;
import com.relic.admin.dto.ResetPasswordDTO;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.vo.AdminVO;

/**
 * 管理员服务接口
 */
public interface SysAdminService extends IService<SysAdmin> {

    /**
     * 分页查询管理员列表 (含角色名称)
     */
    IPage<AdminVO> pageAdminVO(Page<AdminVO> page, String username, Long roleId);

    /**
     * 根据ID查询管理员详情 (含角色名称)
     */
    AdminVO getAdminVOById(Long id);

    /**
     * 创建管理员
     */
    void createAdmin(AdminDTO dto);

    /**
     * 更新管理员
     */
    void updateAdmin(AdminDTO dto);

    /**
     * 删除管理员 (逻辑删除)
     */
    void deleteAdmin(Long id);

    /**
     * 重置管理员密码
     */
    void resetPassword(ResetPasswordDTO dto);

    /**
     * 切换管理员启用/禁用状态
     */
    void toggleStatus(Long id);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);
}
