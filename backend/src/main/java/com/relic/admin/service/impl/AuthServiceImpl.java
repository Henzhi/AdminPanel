package com.relic.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.relic.admin.common.AdminContextHolder;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.LoginDTO;
import com.relic.admin.dto.PasswordDTO;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.entity.SysRole;
import com.relic.admin.mapper.SysAdminMapper;
import com.relic.admin.mapper.SysPermissionMapper;
import com.relic.admin.mapper.SysRoleMapper;
import com.relic.admin.service.AuthService;
import com.relic.admin.service.SysPermissionService;
import com.relic.admin.vo.AdminVO;
import com.relic.admin.vo.LoginVO;
import com.relic.admin.vo.MenuVO;
import com.relic.admin.vo.PermissionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证授权服务实现
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    /** 验证码 Redis Key 前缀 */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final PasswordEncoder passwordEncoder;
    private final SysPermissionService sysPermissionService;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthServiceImpl(SysAdminMapper sysAdminMapper,
                           SysRoleMapper sysRoleMapper,
                           SysPermissionMapper sysPermissionMapper,
                           PasswordEncoder passwordEncoder,
                           SysPermissionService sysPermissionService,
                           StringRedisTemplate stringRedisTemplate) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.passwordEncoder = passwordEncoder;
        this.sysPermissionService = sysPermissionService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1. 校验验证码
        verifyCaptcha(dto);

        // 2. 根据用户名查询管理员
        SysAdmin admin = findByUsername(dto.getUsername());
        if (admin == null || !passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 3. 校验账号状态
        if (admin.getStatus() != Constants.ADMIN_STATUS_ACTIVE) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. Sa-Token 登录
        StpUtil.login(admin.getId());

        // 5. 写入会话信息
        SysRole role = sysRoleMapper.selectById(admin.getRoleId());
        StpUtil.getSession().set("username", admin.getUsername());
        StpUtil.getSession().set("roleId", admin.getRoleId());
        if (role != null) {
            StpUtil.getSession().set("roleCode", role.getRoleCode());
        }

        // 6. 更新最近登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(getClientIp());
        sysAdminMapper.updateById(admin);

        // 7. 组装返回结果
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setAdminId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setRealName(admin.getRealName());
        vo.setAvatar(admin.getAvatar());

        // 角色编码列表
        List<String> roles = new ArrayList<>();
        if (role != null) {
            roles.add(role.getRoleCode());
        }
        vo.setRoles(roles);

        // 权限编码列表
        List<PermissionVO> perms = sysPermissionMapper.selectPermissionsByRoleId(admin.getRoleId());
        List<String> permCodes = perms.stream()
                .map(PermissionVO::getPermCode)
                .distinct()
                .collect(Collectors.toList());
        vo.setPermissions(permCodes);

        log.info("管理员登录成功: id={}, username={}", admin.getId(), admin.getUsername());
        return vo;
    }

    @Override
    public void logout() {
        Object loginId = StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        log.info("管理员登出成功: loginId={}", loginId);
    }

    @Override
    public AdminVO getCurrentAdminInfo() {
        Long adminId = AdminContextHolder.getCurrentAdminId();
        AdminVO vo = sysAdminMapper.selectAdminVOById(adminId);
        if (vo == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public void changePassword(PasswordDTO dto) {
        Long adminId = AdminContextHolder.getCurrentAdminId();
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysAdminMapper.updateById(admin);
        log.info("管理员修改密码成功: id={}", adminId);
    }

    @Override
    public List<MenuVO> getMenuTree() {
        Long adminId = AdminContextHolder.getCurrentAdminId();
        return sysPermissionService.getMenuTreeByAdminId(adminId);
    }

    // ------------------------------------------------------------------
    // 私有辅助方法
    // ------------------------------------------------------------------

    /**
     * 校验验证码 (从 Redis 读取，校验后删除，一次性使用)
     */
    private void verifyCaptcha(LoginDTO dto) {
        String redisKey = CAPTCHA_KEY_PREFIX + dto.getCaptchaKey();
        String stored = stringRedisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(stored)) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR, "验证码已过期或不存在");
        }
        // 一次性使用，校验后立即删除
        stringRedisTemplate.delete(redisKey);
        if (!stored.equalsIgnoreCase(dto.getCaptcha())) {
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
    }

    /**
     * 根据用户名查询管理员
     */
    private SysAdmin findByUsername(String username) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysAdmin> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(SysAdmin::getUsername, username);
        return sysAdminMapper.selectOne(wrapper);
    }

    /**
     * 获取客户端 IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "unknown";
            }
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 多级代理时取第一个
            if (StringUtils.hasText(ip) && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
            return "unknown";
        }
    }
}
