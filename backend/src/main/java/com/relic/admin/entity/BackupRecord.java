package com.relic.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Backup record entity mapped to the {@code backup_record} table.
 *
 * <p>Each row represents a single database backup operation, holding the
 * generated file metadata, the operator who triggered it and the resulting
 * status.</p>
 */
@Data
@TableName("backup_record")
public class BackupRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Backup file name */
    private String filename;

    /** Backup file absolute path */
    private String filePath;

    /** File size in bytes */
    private Long fileSize;

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
