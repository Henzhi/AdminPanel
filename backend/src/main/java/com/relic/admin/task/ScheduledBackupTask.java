package com.relic.admin.task;

import com.relic.admin.service.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that performs an automatic encrypted full backup according to
 * the {@code backup.cron} configuration property.
 *
 * <p>Scheduling is enabled globally via {@code @EnableScheduling} on the
 * application class. The cron expression is read from
 * {@link com.relic.admin.properties.BackupProperties#getCron()}.</p>
 */
@Slf4j
@Component
public class ScheduledBackupTask {

    private final BackupService backupService;

    public ScheduledBackupTask(BackupService backupService) {
        this.backupService = backupService;
    }

    /**
     * Run an encrypted full backup on the configured cron schedule.
     */
    @Scheduled(cron = "${backup.cron}")
    public void executeScheduledBackup() {
        log.info("==== Scheduled backup started ====");
        long start = System.currentTimeMillis();
        try {
            backupService.createBackup(true);
            log.info("==== Scheduled backup finished successfully in {} ms ====",
                    System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("==== Scheduled backup failed after {} ms: {} ====",
                    System.currentTimeMillis() - start, e.getMessage(), e);
        }
    }
}
