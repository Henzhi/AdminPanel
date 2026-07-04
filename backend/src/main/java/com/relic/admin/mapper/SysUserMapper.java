package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relic.admin.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 普通用户 Mapper
 *
 * <p>普通用户无角色关联，分页与详情查询通过 {@link com.relic.admin.service.impl.SysUserServiceImpl}
 * 使用 LambdaQueryWrapper 实现，无需自定义 XML。</p>
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
