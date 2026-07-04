package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * View object for log records, exposing all entity fields plus a formatted
 * create time string suitable for display.
 */
@Data
public class SysLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Log type: OPERATION, SYSTEM, SECURITY */
    private String logType;

    /** Operator admin ID */
    private Long operatorId;

    /** Operator admin name */
    private String operatorName;

    /** Operation type */
    private String operationType;

    /** Operation target */
    private String operationTarget;

    /** Java method name */
    private String method;

    /** Request URL */
    private String requestUrl;

    /** HTTP method */
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
    private LocalDateTime createTime;

    /** Formatted create time string, e.g. yyyy-MM-dd HH:mm:ss */
    private String formattedCreateTime;
}
