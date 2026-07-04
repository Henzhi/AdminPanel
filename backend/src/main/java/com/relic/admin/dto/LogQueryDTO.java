package com.relic.admin.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Query parameters for log pagination and filtering.
 */
@Data
public class LogQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Page number, 1-based */
    private Integer page = 1;

    /** Page size */
    private Integer size = 10;

    /** Log type: OPERATION, SYSTEM, SECURITY */
    private String logType;

    /** Operation type: CREATE, UPDATE, DELETE, LOGIN, etc. */
    private String operationType;

    /** Operator admin name (fuzzy match) */
    private String operatorName;

    /** Start date of create time range, format: yyyy-MM-dd HH:mm:ss */
    private String startDate;

    /** End date of create time range, format: yyyy-MM-dd HH:mm:ss */
    private String endDate;

    /** Keyword to search operation_target and request_url (fuzzy match) */
    private String keyword;
}
