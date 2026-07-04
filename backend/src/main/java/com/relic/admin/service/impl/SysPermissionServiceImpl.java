package com.relic.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.ResultCode;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.entity.SysPermission;
import com.relic.admin.mapper.SysAdminMapper;
import com.relic.admin.mapper.SysPermissionMapper;
import com.relic.admin.service.SysPermissionService;
import com.relic.admin.vo.MenuVO;
import com.relic.admin.vo.PermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysAdminMapper sysAdminMapper;

    public SysPermissionServiceImpl(SysPermissionMapper sysPermissionMapper, SysAdminMapper sysAdminMapper) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysAdminMapper = sysAdminMapper;
    }

    @Override
    public List<SysPermission> getAllPermissions() {
        return sysPermissionMapper.selectAllPermissions();
    }

    @Override
    public List<PermissionVO> getPermissionTree() {
        List<SysPermission> all = getAllPermissions();
        List<PermissionVO> voList = all.stream().map(this::toPermissionVO).collect(Collectors.toList());
        return buildPermissionTree(voList);
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(Long roleId) {
        return sysPermissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    public List<MenuVO> getMenuTreeByAdminId(Long adminId) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        // 获取该角色拥有的全部权限，仅保留菜单类型 (perm_type=1)
        List<PermissionVO> perms = sysPermissionMapper.selectPermissionsByRoleId(admin.getRoleId());
        List<MenuVO> menus = perms.stream()
                .filter(p -> Constants.PERM_TYPE_MENU == p.getPermType())
                .map(this::toMenuVO)
                .collect(Collectors.toList());
        return buildMenuTree(menus);
    }

    // ------------------------------------------------------------------
    // 私有辅助方法
    // ------------------------------------------------------------------

    private PermissionVO toPermissionVO(SysPermission p) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(p, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private MenuVO toMenuVO(PermissionVO p) {
        MenuVO vo = new MenuVO();
        vo.setId(p.getId());
        vo.setParentId(p.getParentId());
        vo.setName(p.getPermName());
        vo.setPath(p.getPath());
        vo.setComponent(p.getComponent());
        vo.setIcon(p.getIcon());
        vo.setSortOrder(p.getSortOrder());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * 构建权限树 (parent_id=0 为根节点)
     */
    private List<PermissionVO> buildPermissionTree(List<PermissionVO> all) {
        Map<Long, PermissionVO> map = all.stream()
                .collect(Collectors.toMap(PermissionVO::getId, v -> v, (a, b) -> a));
        List<PermissionVO> roots = new ArrayList<>();
        for (PermissionVO vo : all) {
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
            } else {
                PermissionVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 找不到父节点，作为根节点处理
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    /**
     * 构建菜单树 (parent_id=0 为根节点)
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> all) {
        Map<Long, MenuVO> map = all.stream()
                .collect(Collectors.toMap(MenuVO::getId, v -> v, (a, b) -> a));
        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : all) {
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
            } else {
                MenuVO parent = map.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }
}
