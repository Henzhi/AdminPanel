package com.relic.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Unified system log entity (operation + system + security).
 *
 * <p>Maps to the {@code sys_log} table. A single table stores all three log
 * categories distinguished by the {@link #logType} field.</p>
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** Log type: OPERATION, SYSTEM, SECURITY */
    private String logType;

    /** Operator admin ID */
    private Long operatorId;

    /** Operator admin name */
    private String operatorName;

    /** Operation type: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, BACKUP, RESTORE, etc. */
    private String operationType;

    /** Operation target: table or module name */
    private String operationTarget;

    /** Java method name */
    private String method;

    /** Request URL */
    private String requestUrl;

    /** HTTP method: GET, POST, PUT, DELETE */
    private String requestMethod;

    /** Request parameters (JSON) */
    private String requestParams;

    /** Data before operation (JSON) */
    private String beforeData;

    /** Data after operation (JSON) */
    private String afterData;

    /** Request IP */
    private String ip;

    /** Status: 1=success, 0=error */
    private Integer status;

    /** Error message if failed */
    private String errorMsg;

    /** Cost time in milliseconds */
    private Long costTime;

    /** Create time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
