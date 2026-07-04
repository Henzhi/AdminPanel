package com.relic.admin.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Query parameters for paginated backup record listing.
 */
@Data
public class BackupQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Page number (1-based) */
    private Integer page = 1;

    /** Page size */
    private Integer size = 10;

    /** Backup type filter, e.g. FULL */
    private String backupType;

    /** Start date of the create-time range, format yyyy-MM-dd */
    private String startDate;

    /** End date of the create-time range, format yyyy-MM-dd */
    private String endDate;

    /** Operator name filter (fuzzy match) */
    private String operatorName;
}
