package com.relic.admin.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.mapper.SysAdminMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据初始化器
 *
 * <p>系统启动时检查三个默认管理员账号 (admin / auditor / dataadmin) 的密码是否仍为
 * SQL 脚本中的占位哈希。若是，则使用 PasswordEncoder (BCrypt) 重新哈希正确的明文密码
 * 并更新到数据库，使账号可正常登录。</p>
 *
 * <p>占位哈希 {@value #PLACEHOLDER_HASH} 是一个合法的 BCrypt 格式字符串，仅用于满足
 * NOT NULL 约束，并非任何明文密码的真实哈希。</p>
 */
@Slf4j
@Order(1)
@Component
public class DataInitializer implements CommandLineRunner {

    /** SQL 脚本中使用的占位密码哈希 */
    private static final String PLACEHOLDER_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    /** 默认管理员账号 -> 正确明文密码 */
    private static final Map<String, String> DEFAULT_ADMIN_PASSWORDS = new LinkedHashMap<>();

    static {
        DEFAULT_ADMIN_PASSWORDS.put("admin", "Admin@123");
        DEFAULT_ADMIN_PASSWORDS.put("auditor", "Audi@123");
        DEFAULT_ADMIN_PASSWORDS.put("dataadmin", "Data@123");
    }

    private final SysAdminMapper sysAdminMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(SysAdminMapper sysAdminMapper, PasswordEncoder passwordEncoder) {
        this.sysAdminMapper = sysAdminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        int rehashed = 0;
        for (Map.Entry<String, String> entry : DEFAULT_ADMIN_PASSWORDS.entrySet()) {
            String username = entry.getKey();
            String correctPassword = entry.getValue();

            LambdaQueryWrapper<SysAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysAdmin::getUsername, username);
            SysAdmin admin = sysAdminMapper.selectOne(wrapper);
            if (admin == null) {
                log.warn("DataInitializer: 默认管理员 '{}' 不存在，跳过", username);
                continue;
            }

            String current = admin.getPassword();
            if (needsRehash(current)) {
                String newHash = passwordEncoder.encode(correctPassword);
                admin.setPassword(newHash);
                sysAdminMapper.updateById(admin);
                rehashed++;
                log.info("DataInitializer: 已为管理员 '{}' 重新哈希密码", username);
            }
        }

        if (rehashed > 0) {
            log.info("DataInitializer: 共重新哈希 {} 个默认管理员密码", rehashed);
        } else {
            log.info("DataInitializer: 所有默认管理员密码均已正确哈希，无需处理");
        }
    }

    /**
     * 判断密码是否需要重新哈希：
     * <ul>
     *   <li>密码为空；或</li>
     *   <li>密码仍为 SQL 脚本中的占位哈希。</li>
     * </ul>
     */
    private boolean needsRehash(String current) {
        if (current == null || current.trim().isEmpty()) {
            return true;
        }
        return PLACEHOLDER_HASH.equals(current);
    }
}
