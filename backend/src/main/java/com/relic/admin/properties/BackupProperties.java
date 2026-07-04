package com.relic.admin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Backup configuration properties bound to the {@code backup} prefix.
 */
@Data
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {

    /** Local path where backup files are stored */
    private String path;

    /** AES secret key used to encrypt/decrypt backup files */
    private String aesKey;

    /** Cron expression controlling the scheduled backup job */
    private String cron;
}
