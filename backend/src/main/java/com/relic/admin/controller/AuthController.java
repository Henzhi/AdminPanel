package com.relic.admin.controller;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.relic.admin.annotation.OperLog;
import com.relic.admin.common.Constants;
import com.relic.admin.common.Result;
import com.relic.admin.dto.LoginDTO;
import com.relic.admin.dto.PasswordDTO;
import com.relic.admin.service.AuthService;
import com.relic.admin.vo.AdminVO;
import com.relic.admin.vo.LoginVO;
import com.relic.admin.vo.MenuVO;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证授权控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TIMEOUT = 5;

    public AuthController(AuthService authService, StringRedisTemplate stringRedisTemplate) {
        this.authService = authService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    @OperLog(logType = Constants.LOG_TYPE_SECURITY, operationType = Constants.OP_LOGIN, operationTarget = "auth")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    @OperLog(logType = Constants.LOG_TYPE_SECURITY, operationType = Constants.OP_LOGOUT, operationTarget = "auth")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /**
     * 获取当前登录管理员信息
     */
    @GetMapping("/info")
    public Result<AdminVO> info() {
        return Result.success(authService.getCurrentAdminInfo());
    }

    /**
     * 修改自身密码
     */
    @PutMapping("/password")
    @OperLog(logType = Constants.LOG_TYPE_SECURITY, operationType = Constants.OP_PASSWORD_RESET, operationTarget = "auth")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }

    /**
     * 获取当前管理员的动态菜单树
     */
    @GetMapping("/menus")
    public Result<List<MenuVO>> menus() {
        return Result.success(authService.getMenuTree());
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        LineCaptcha captcha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(120, 40, 4, 30);
        String code = captcha.getCode();
        String key = IdUtil.fastSimpleUUID();

        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, code, CAPTCHA_TIMEOUT, TimeUnit.MINUTES);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        captcha.write(bos);
        String base64Image = Base64.getEncoder().encodeToString(bos.toByteArray());
        IoUtil.close(bos);

        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", "data:image/png;base64," + base64Image);
        return Result.success(result);
    }
}
