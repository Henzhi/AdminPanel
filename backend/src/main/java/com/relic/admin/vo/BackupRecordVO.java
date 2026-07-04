package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * View object for backup record details, adding a human-readable file size.
 */
@Data
public class BackupRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Backup file name */
    private String filename;

    /** Backup file absolute path */
    private String filePath;

    /** File size in bytes */
    private Long fileSize;

    /** Human-readable file size, e.g. "1.5 MB" */
    private String formattedFileSize;

    /** Backup type, e.g. FULL */
    private String backupType;

    /** Encrypted: 0=no, 1=yes */
    private Integer isEncrypted;

    /** Admin ID who triggered the backup */
    private Long operatorId;

    /** Admin name who triggered the backup */
    private String operatorName;

    /** Status: 1=success, 0=failed */
    private Integer status;

    /** Remark / error message */
    private String remark;

    /** Create time */
    private LocalDateTime createTime;

    /** Update time */
    private LocalDateTime updateTime;
}
