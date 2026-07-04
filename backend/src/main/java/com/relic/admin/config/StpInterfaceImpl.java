package com.relic.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.entity.SysRole;
import com.relic.admin.mapper.SysAdminMapper;
import com.relic.admin.mapper.SysPermissionMapper;
import com.relic.admin.mapper.SysRoleMapper;
import com.relic.admin.vo.PermissionVO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限/角色数据源实现
 *
 * <p>为 Sa-Token 提供 {@code getPermissionList} 与 {@code getRoleList} 的具体逻辑，
 * 使 {@code @SaCheckPermission} / {@code @SaCheckRole} 注解生效。</p>
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public StpInterfaceImpl(SysAdminMapper sysAdminMapper,
                            SysRoleMapper sysRoleMapper,
                            SysPermissionMapper sysPermissionMapper) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    /**
     * 返回当前登录管理员所拥有角色的全部权限编码
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long adminId = parseLoginId(loginId);
        if (adminId == null) {
            return Collections.emptyList();
        }
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            return Collections.emptyList();
        }
        List<PermissionVO> perms = sysPermissionMapper.selectPermissionsByRoleId(admin.getRoleId());
        return perms.stream()
                .map(PermissionVO::getPermCode)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 返回当前登录管理员的角色编码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long adminId = parseLoginId(loginId);
        if (adminId == null) {
            return Collections.emptyList();
        }
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            return Collections.emptyList();
        }
        SysRole role = sysRoleMapper.selectById(admin.getRoleId());
        if (role == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(role.getRoleCode());
    }

    private Long parseLoginId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.parseLong(loginId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
