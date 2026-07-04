package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.vo.AdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员 Mapper
 */
@Mapper
public interface SysAdminMapper extends BaseMapper<SysAdmin> {

    /**
     * 根据ID查询管理员详情 (含角色名称)
     */
    AdminVO selectAdminVOById(Long id);

    /**
     * 分页查询管理员列表 (含角色名称)
     */
    IPage<AdminVO> selectAdminVOPage(Page<AdminVO> page, @Param("username") String username, @Param("roleId") Long roleId);
}
