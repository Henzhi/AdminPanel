package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relic.admin.entity.SysPermission;
import com.relic.admin.vo.PermissionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据角色ID查询其拥有的权限列表
     */
    List<PermissionVO> selectPermissionsByRoleId(Long roleId);

    /**
     * 查询全部权限 (按 sort_order 排序)
     */
    List<SysPermission> selectAllPermissions();
}
