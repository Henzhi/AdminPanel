package com.relic.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relic.admin.common.AdminContextHolder;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.BackupQueryDTO;
import com.relic.admin.dto.RestoreDTO;
import com.relic.admin.entity.BackupRecord;
import com.relic.admin.entity.SysAdmin;
import com.relic.admin.mapper.BackupRecordMapper;
import com.relic.admin.properties.BackupProperties;
import com.relic.admin.service.BackupService;
import com.relic.admin.service.SysAdminService;
import com.relic.admin.util.AesUtil;
import com.relic.admin.vo.BackupRecordVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BackupService}.
 *
 * <p>Backups are produced by invoking the {@code mysqldump} client tool via
 * {@link ProcessBuilder} and restores by invoking the {@code mysql} client.
 * Because {@link ProcessBuilder} does not interpret shell redirection ({@code >}
 * / {@code <}), the dump stdout is streamed into the target file and the
 * restore file is streamed into the mysql process stdin.</p>
 */
@Slf4j
@Service
public class BackupServiceImpl implements BackupService {

    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?/([^?]+)");

    private final BackupRecordMapper backupRecordMapper;
    private final BackupProperties backupProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final SysAdminService sysAdminService;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    public BackupServiceImpl(BackupRecordMapper backupRecordMapper,
                             BackupProperties backupProperties,
                             RedisTemplate<String, Object> redisTemplate,
                             PasswordEncoder passwordEncoder,
                             SysAdminService sysAdminService) {
        this.backupRecordMapper = backupRecordMapper;
        this.backupProperties = backupProperties;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.sysAdminService = sysAdminService;
    }

    // ------------------------------------------------------------------
    // Query operations
    // ------------------------------------------------------------------

    @Override
    public PageResult<BackupRecordVO> getBackupPage(BackupQueryDTO query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();

        Page<BackupRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<BackupRecord> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getBackupType())) {
            wrapper.eq(BackupRecord::getBackupType, query.getBackupType());
        }
        if (StringUtils.hasText(query.getOperatorName())) {
            wrapper.like(BackupRecord::getOperatorName, query.getOperatorName());
        }
        if (StringUtils.hasText(query.getStartDate())) {
            LocalDate start = LocalDate.parse(query.getStartDate());
            wrapper.ge(BackupRecord::getCreateTime, start.atStartOfDay());
        }
        if (StringUtils.hasText(query.getEndDate())) {
            LocalDate end = LocalDate.parse(query.getEndDate());
            wrapper.le(BackupRecord::getCreateTime, end.atTime(LocalTime.MAX));
        }
        wrapper.orderByDesc(BackupRecord::getCreateTime);

        Page<BackupRecord> result = backupRecordMapper.selectPage(pageParam, wrapper);
        List<BackupRecordVO> voList = result.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), voList);
    }

    @Override
    public BackupRecordVO getBackupDetail(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.BACKUP_FILE_NOT_FOUND);
        }
        return toVO(record);
    }

    // ------------------------------------------------------------------
    // Backup
    // ------------------------------------------------------------------

    @Override
    public BackupRecord createBackup(boolean encrypt) {
        String timestamp = LocalDateTime.now().format(FILENAME_FORMATTER);
        String filename = "relic_admin_" + timestamp + ".sql";

        Path backupDir = Paths.get(backupProperties.getPath());
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            log.error("Failed to create backup directory: {}", backupDir, e);
            throw new BusinessException(ResultCode.BACKUP_FAILED, "无法创建备份目录: " + e.getMessage());
        }

        Path sqlPath = backupDir.resolve(filename);
        LocalDateTime now = LocalDateTime.now();

        BackupRecord record = new BackupRecord();
        record.setFilename(filename);
        record.setFilePath(sqlPath.toString());
        record.setBackupType(Constants.BACKUP_TYPE_FULL);
        record.setIsEncrypted(encrypt ? 1 : 0);
        record.setOperatorId(safeGetCurrentAdminId());
        record.setOperatorName(safeGetCurrentAdminName());
        record.setCreateTime(now);
        record.setUpdateTime(now);

        try {
            // 1. Execute mysqldump, streaming stdout into the target file.
            executeMysqldump(sqlPath);

            Path finalPath = sqlPath;
            String finalFilename = filename;

            // 2. Optionally AES-encrypt the dump file.
            if (encrypt) {
                Path encPath = Paths.get(sqlPath.toString() + ".enc");
                AesUtil.encryptFile(sqlPath.toFile(), encPath.toFile(), backupProperties.getAesKey());
                Files.deleteIfExists(sqlPath);
                finalPath = encPath;
                finalFilename = filename + ".enc";
                record.setFilename(finalFilename);
                record.setFilePath(finalPath.toString());
            }

            long fileSize = Files.size(finalPath);
            record.setFileSize(fileSize);
            record.setStatus(1);
            record.setRemark(encrypt ? "加密全量备份完成" : "全量备份完成");
            backupRecordMapper.insert(record);

            log.info("Backup created successfully: {} ({} bytes) by {}",
                    finalFilename, fileSize, record.getOperatorName());
            return record;
        } catch (Exception e) {
            log.error("Backup failed for file: {}", filename, e);
            record.setStatus(0);
            record.setFileSize(0L);
            record.setRemark("备份失败: " + e.getMessage());
            backupRecordMapper.insert(record);
            throw new BusinessException(ResultCode.BACKUP_FAILED, "备份失败: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Restore
    // ------------------------------------------------------------------

    @Override
    public void restoreBackup(RestoreDTO dto) {
        // 1. Locate the backup record and verify the file exists.
        BackupRecord record = backupRecordMapper.selectById(dto.getBackupId());
        if (record == null) {
            throw new BusinessException(ResultCode.BACKUP_FILE_NOT_FOUND);
        }
        Path filePath = Paths.get(record.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException(ResultCode.BACKUP_FILE_NOT_FOUND);
        }

        // 2. Verify the password confirmation against the current admin.
        Long adminId = AdminContextHolder.getCurrentAdminId();
        SysAdmin admin = sysAdminService.getById(adminId);
        if (admin == null || !passwordEncoder.matches(dto.getConfirmPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.RESTORE_CONFIRM_ERROR);
        }

        // 3. Only SUPER_ADMIN may perform a restore.
        if (!StpUtil.hasRole("SUPER_ADMIN")) {
            throw new BusinessException(ResultCode.PERMISSION_DENIED, "仅超级管理员可执行恢复操作");
        }

        // 4. Enter maintenance mode for the duration of the restore.
        redisTemplate.opsForValue().set(Constants.SYSTEM_MAINTAINING_KEY, Boolean.TRUE);
        log.warn("System entered maintenance mode for restore of backup id={} by admin={}",
                dto.getBackupId(), admin.getUsername());

        Path tempFile = null;
        try {
            Path inputFile = filePath;

            // 5. If encrypted, decrypt to a temporary file first.
            if (record.getIsEncrypted() != null && record.getIsEncrypted() == 1) {
                tempFile = Files.createTempFile("restore_", ".sql");
                AesUtil.decryptFile(filePath.toFile(), tempFile.toFile(), backupProperties.getAesKey());
                inputFile = tempFile;
            }

            // 6. Execute mysql restore, streaming the file into stdin.
            executeMysqlRestore(inputFile);

            log.info("Restore completed successfully for backup id={} by admin={}",
                    dto.getBackupId(), admin.getUsername());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Restore failed for backup id={}", dto.getBackupId(), e);
            throw new BusinessException(ResultCode.RESTORE_FAILED, "恢复失败: " + e.getMessage());
        } finally {
            // 7. Always leave maintenance mode.
            redisTemplate.delete(Constants.SYSTEM_MAINTAINING_KEY);
            log.info("System maintenance mode cleared after restore of backup id={}", dto.getBackupId());

            // Clean up the temporary decrypted file.
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary restore file: {}", tempFile, e);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Download
    // ------------------------------------------------------------------

    @Override
    public void downloadBackup(Long id, HttpServletResponse response) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.BACKUP_FILE_NOT_FOUND);
        }
        Path filePath = Paths.get(record.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BusinessException(ResultCode.BACKUP_FILE_NOT_FOUND);
        }

        response.setContentType("application/octet-stream");
        String encodedFilename = URLEncoder.encode(record.getFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
        if (record.getFileSize() != null) {
            response.setContentLengthLong(record.getFileSize());
        }

        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
            out.flush();
        } catch (IOException e) {
            log.error("Failed to stream backup file id={} for download", id, e);
            throw new BusinessException(ResultCode.BACKUP_FAILED, "下载备份文件失败: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Process execution helpers
    // ------------------------------------------------------------------

    /**
     * Run {@code mysqldump} and stream its stdout into the given output file.
     *
     * <p>{@link ProcessBuilder} does not support shell redirection, so the
     * {@code > file} part of the documented command is implemented here by
     * piping the process stdout directly into the file.</p>
     */
    private void executeMysqldump(Path outputFile) throws IOException, InterruptedException {
        String[] conn = parseMysqlConnection();
        String host = conn[0];
        String port = conn[1];
        String database = conn[2];

        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("--skip-ssl");
        command.add("-h" + host);
        if (port != null) {
            command.add("-P" + port);
        }
        command.add("-u" + datasourceUsername);
        command.add("-p" + datasourcePassword);
        command.add(database);

        Process process = new ProcessBuilder(command).redirectErrorStream(false).start();

        // Read stderr on a separate thread to avoid blocking when the stdout
        // pipe fills up.
        StringBuilder errorBuilder = new StringBuilder();
        Thread errorThread = drainStream(process.getErrorStream(), errorBuilder);

        try (InputStream in = process.getInputStream();
             OutputStream out = Files.newOutputStream(outputFile)) {
            in.transferTo(out);
        }

        errorThread.join();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("mysqldump exited with code " + exitCode + ": " + errorBuilder);
        }
    }

    /**
     * Run {@code mysql} and stream the given input file into its stdin.
     *
     * <p>{@link ProcessBuilder} does not support shell redirection, so the
     * {@code < file} part of the documented command is implemented here by
     * piping the file directly into the process stdin.</p>
     */
    private void executeMysqlRestore(Path inputFile) throws IOException, InterruptedException {
        String[] conn = parseMysqlConnection();
        String host = conn[0];
        String port = conn[1];
        String database = conn[2];

        List<String> command = new ArrayList<>();
        command.add("mysql");
        command.add("--skip-ssl");
        command.add("-h" + host);
        if (port != null) {
            command.add("-P" + port);
        }
        command.add("-u" + datasourceUsername);
        command.add("-p" + datasourcePassword);
        command.add(database);

        Process process = new ProcessBuilder(command).redirectErrorStream(false).start();

        StringBuilder errorBuilder = new StringBuilder();
        Thread errorThread = drainStream(process.getErrorStream(), errorBuilder);

        // Writing to stdin and closing it signals EOF to the mysql client.
        try (OutputStream out = process.getOutputStream();
             InputStream in = Files.newInputStream(inputFile)) {
            in.transferTo(out);
            out.flush();
        }

        errorThread.join();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("mysql restore exited with code " + exitCode + ": " + errorBuilder);
        }
    }

    /**
     * Read a process stream fully into the given buffer on a background thread.
     * Returns the started thread so the caller can {@link Thread#join()} it.
     */
    private Thread drainStream(InputStream stream, StringBuilder target) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    target.append(line).append('\n');
                }
            } catch (IOException e) {
                target.append("Error reading process stream: ").append(e.getMessage());
            }
        }, "backup-process-stream-drain");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Parse host, port and database from the JDBC URL.
     *
     * @return {@code [host, port (nullable), database]}
     */
    private String[] parseMysqlConnection() {
        Matcher matcher = JDBC_URL_PATTERN.matcher(datasourceUrl);
        if (!matcher.find()) {
            throw new BusinessException(ResultCode.BACKUP_FAILED,
                    "无法解析数据库连接URL: " + datasourceUrl);
        }
        String host = matcher.group(1);
        String port = matcher.group(2);
        String database = matcher.group(3);
        return new String[]{host, port, database};
    }

    // ------------------------------------------------------------------
    // Misc helpers
    // ------------------------------------------------------------------

    /**
     * Convert a byte count into a human-readable string (B, KB, MB, GB).
     */
    private String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();
        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }
        if (unitIndex == 0) {
            return (long) fileSize + " " + units[unitIndex];
        }
        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }

    private BackupRecordVO toVO(BackupRecord record) {
        BackupRecordVO vo = new BackupRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setFormattedFileSize(formatFileSize(record.getFileSize()));
        return vo;
    }

    /**
     * Safely obtain the current admin id, falling back to 0 when no admin is
     * logged in (e.g. during a scheduled backup).
     */
    private Long safeGetCurrentAdminId() {
        try {
            if (AdminContextHolder.isLogin()) {
                return AdminContextHolder.getCurrentAdminId();
            }
        } catch (Exception e) {
            log.debug("No admin in context, using system operator");
        }
        return 0L;
    }

    /**
     * Safely obtain the current admin name, falling back to "SYSTEM" when no
     * admin is logged in (e.g. during a scheduled backup).
     */
    private String safeGetCurrentAdminName() {
        try {
            if (AdminContextHolder.isLogin()) {
                return AdminContextHolder.getCurrentAdminName();
            }
        } catch (Exception e) {
            log.debug("No admin in context, using system operator");
        }
        return "SYSTEM";
    }
}
