package com.relic.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.RoleDTO;
import com.relic.admin.entity.SysRole;
import com.relic.admin.entity.SysRolePermission;
import com.relic.admin.mapper.SysRoleMapper;
import com.relic.admin.mapper.SysRolePermissionMapper;
import com.relic.admin.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    public SysRoleServiceImpl(SysRoleMapper sysRoleMapper, SysRolePermissionMapper sysRolePermissionMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
    }

    @Override
    public IPage<SysRole> pageRoles(Page<SysRole> page, String roleName) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roleName)) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return sysRoleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO dto) {
        // 校验角色编码唯一
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
        if (count(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setStatus(Constants.ADMIN_STATUS_ACTIVE);
        save(role);
        assignPermissions(role.getId(), dto.getPermissionIds());
        log.info("创建角色成功: {}", dto.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO dto) {
        SysRole role = getById(dto.getId());
        if (role == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        // 编码变更时校验唯一性
        if (!role.getRoleCode().equals(dto.getRoleCode())) {
            LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysRole::getRoleCode, dto.getRoleCode());
            if (count(wrapper) > 0) {
                throw new BusinessException("角色编码已存在");
            }
        }
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        updateById(role);
        assignPermissions(role.getId(), dto.getPermissionIds());
        log.info("更新角色成功: id={}, code={}", role.getId(), role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "角色不存在");
        }
        // 清理角色-权限关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, id);
        sysRolePermissionMapper.delete(wrapper);
        log.info("删除角色成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先删除原有关联
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        sysRolePermissionMapper.delete(wrapper);
        // 再插入新关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                sysRolePermissionMapper.insert(rp);
            }
        }
        log.info("为角色分配权限成功: roleId={}, permissionCount={}", roleId,
                permissionIds == null ? 0 : permissionIds.size());
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        LambdaQueryWrapper<SysRolePermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRolePermission::getRoleId, roleId);
        List<SysRolePermission> list = sysRolePermissionMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }
}
