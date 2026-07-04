package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.relic.admin.annotation.OperLog;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.dto.BackupQueryDTO;
import com.relic.admin.dto.RestoreDTO;
import com.relic.admin.entity.BackupRecord;
import com.relic.admin.service.BackupService;
import com.relic.admin.vo.BackupRecordVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for the backup &amp; recovery module.
 *
 * <p>All endpoints live under {@code /api/admin/backups} and are protected by
 * Sa-Token permission/role checks.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/backups")
public class BackupController {

    private final BackupService backupService;
    private final RedisTemplate<String, Object> redisTemplate;

    public BackupController(BackupService backupService,
                            RedisTemplate<String, Object> redisTemplate) {
        this.backupService = backupService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Paginated list of backup records with optional filters.
     */
    @GetMapping
    @SaCheckPermission("backup:list")
    public Result<PageResult<BackupRecordVO>> list(BackupQueryDTO query) {
        return Result.success(backupService.getBackupPage(query));
    }

    /**
     * Get the detail of a single backup record.
     */
    @GetMapping("/{id}")
    @SaCheckPermission("backup:list")
    public Result<BackupRecordVO> detail(@PathVariable Long id) {
        return Result.success(backupService.getBackupDetail(id));
    }

    /**
     * Trigger a manual full backup.
     *
     * @param encrypt whether the resulting file should be AES-encrypted
     */
    @PostMapping
    @SaCheckPermission("backup:create")
    @OperLog(logType = Constants.LOG_TYPE_SYSTEM, operationType = Constants.OP_BACKUP, operationTarget = "database")
    public Result<BackupRecord> create(@RequestParam(defaultValue = "false") boolean encrypt) {
        log.info("Manual backup triggered, encrypt={}", encrypt);
        return Result.success(backupService.createBackup(encrypt));
    }

    /**
     * Restore the database from a backup. Restricted to SUPER_ADMIN and
     * requires password confirmation.
     */
    @PostMapping("/{id}/restore")
    @SaCheckPermission("backup:restore")
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(logType = Constants.LOG_TYPE_SYSTEM, operationType = Constants.OP_RESTORE, operationTarget = "database")
    public Result<Void> restore(@PathVariable Long id, @Valid @RequestBody RestoreDTO dto) {
        dto.setBackupId(id);
        backupService.restoreBackup(dto);
        return Result.success();
    }

    /**
     * Download a backup file.
     */
    @GetMapping("/{id}/download")
    @SaCheckPermission("backup:download")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        backupService.downloadBackup(id, response);
    }

    /**
     * Check whether the system is currently in maintenance mode (i.e. a
     * restore is in progress).
     */
    @GetMapping("/status")
    public Result<Map<String, Boolean>> status() {
        Boolean maintaining = Boolean.TRUE.equals(
                redisTemplate.hasKey(Constants.SYSTEM_MAINTAINING_KEY));
        Map<String, Boolean> result = new HashMap<>();
        result.put("maintaining", maintaining);
        return Result.success(result);
    }
}
