package com.relic.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.ResetUserPasswordDTO;
import com.relic.admin.dto.UserDTO;
import com.relic.admin.entity.SysUser;
import com.relic.admin.mapper.SysUserMapper;
import com.relic.admin.service.SysUserService;
import com.relic.admin.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 普通用户服务实现
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserVO> pageUserVO(Integer page, Integer size, String username, Integer status) {
        Page<SysUser> p = new Page<>(page, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = page(p, wrapper);
        List<UserVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    public UserVO getUserVOById(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        return toVO(user);
    }

    @Override
    public void createUser(UserDTO dto) {
        if (checkUsernameExists(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender() == null ? 0 : dto.getGender());
        user.setStatus(Constants.ADMIN_STATUS_ACTIVE);
        save(user);
        log.info("创建普通用户成功: {}", dto.getUsername());
    }

    @Override
    public void updateUser(UserDTO dto) {
        SysUser user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        // 用户名变更时校验唯一性
        if (!user.getUsername().equals(dto.getUsername()) && checkUsernameExists(dto.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        user.setUsername(dto.getUsername());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        updateById(user);
        log.info("更新普通用户成功: id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    public void deleteUser(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        log.info("删除普通用户成功: id={}", id);
    }

    @Override
    public void resetPassword(ResetUserPasswordDTO dto) {
        SysUser user = getById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
        log.info("重置普通用户密码成功: id={}", dto.getUserId());
    }

    @Override
    public void toggleStatus(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() == Constants.ADMIN_STATUS_ACTIVE) {
            user.setStatus(Constants.ADMIN_STATUS_DISABLED);
        } else {
            user.setStatus(Constants.ADMIN_STATUS_ACTIVE);
        }
        updateById(user);
        log.info("切换普通用户状态成功: id={}, status={}", id, user.getStatus());
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return count(wrapper) > 0;
    }

    /**
     * 实体转 VO (自动忽略 password 字段)
     */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
