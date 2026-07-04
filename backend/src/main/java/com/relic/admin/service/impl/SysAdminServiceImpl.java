package com.relic.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.AdminDTO;
import com.relic.admin.dto.ResetPasswordDTO;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.mapper.SysAdminMapper;
import com.relic.admin.service.SysAdminService;
import com.relic.admin.vo.AdminVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理员服务实现
 */
@Slf4j
@Service
public class SysAdminServiceImpl extends ServiceImpl<SysAdminMapper, SysAdmin> implements SysAdminService {

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;

    public SysAdminServiceImpl(SysAdminMapper sysAdminMapper, PasswordEncoder passwordEncoder) {
        this.sysAdminMapper = sysAdminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public IPage<AdminVO> pageAdminVO(Page<AdminVO> page, String username, Long roleId) {
        return sysAdminMapper.selectAdminVOPage(page, username, roleId);
    }

    @Override
    public AdminVO getAdminVOById(Long id) {
        AdminVO vo = sysAdminMapper.selectAdminVOById(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public void createAdmin(AdminDTO dto) {
        if (checkUsernameExists(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        SysAdmin admin = new SysAdmin();
        admin.setUsername(dto.getUsername());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setRealName(dto.getRealName());
        admin.setEmail(dto.getEmail());
        admin.setPhone(dto.getPhone());
        admin.setRoleId(dto.getRoleId());
        admin.setStatus(Constants.ADMIN_STATUS_ACTIVE);
        save(admin);
        log.info("创建管理员成功: {}", dto.getUsername());
    }

    @Override
    public void updateAdmin(AdminDTO dto) {
        SysAdmin admin = getById(dto.getId());
        if (admin == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        // 用户名变更时校验唯一性
        if (!admin.getUsername().equals(dto.getUsername()) && checkUsernameExists(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        admin.setUsername(dto.getUsername());
        // 密码非空才更新
        if (StringUtils.hasText(dto.getPassword())) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        admin.setRealName(dto.getRealName());
        admin.setEmail(dto.getEmail());
        admin.setPhone(dto.getPhone());
        admin.setRoleId(dto.getRoleId());
        updateById(admin);
        log.info("更新管理员成功: id={}, username={}", admin.getId(), admin.getUsername());
    }

    @Override
    public void deleteAdmin(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        log.info("删除管理员成功: id={}", id);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        SysAdmin admin = getById(dto.getAdminId());
        if (admin == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(admin);
        log.info("重置管理员密码成功: id={}", dto.getAdminId());
    }

    @Override
    public void toggleStatus(Long id) {
        SysAdmin admin = getById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        if (admin.getStatus() == Constants.ADMIN_STATUS_ACTIVE) {
            admin.setStatus(Constants.ADMIN_STATUS_DISABLED);
        } else {
            admin.setStatus(Constants.ADMIN_STATUS_ACTIVE);
        }
        updateById(admin);
        log.info("切换管理员状态成功: id={}, status={}", id, admin.getStatus());
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getUsername, username);
        return count(wrapper) > 0;
    }
}
