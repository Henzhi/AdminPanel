package com.relic.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.common.PageResult;
import com.relic.admin.dto.LogQueryDTO;
import com.relic.admin.entity.SysLog;

import java.io.OutputStream;

/**
 * Service interface for system log management.
 *
 * <p>Provides paginated querying, asynchronous log recording (both raw and
 * convenience variants), and CSV export of log records.</p>
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * Paginated log query with multi-condition filtering.
     *
     * @param query the query parameters (page, size, filters)
     * @return paginated result containing matching log records
     */
    PageResult<SysLog> getLogPage(LogQueryDTO query);

    /**
     * Persist a fully-built log record asynchronously.
     *
     * @param sysLog the log record to save
     */
    void recordLog(SysLog sysLog);

    /**
     * Convenience method that builds and persists a log record.
     *
     * <p>Operator information is read from {@link com.relic.admin.common.AdminContextHolder}
     * (handling the case where no user is logged in, e.g. a failed login
     * attempt). Request URL, HTTP method and client IP are captured from the
     * current HTTP request context when available; for non-HTTP contexts
     * (e.g. scheduled tasks) these fields are left null.</p>
     *
     * @param logType         log type: OPERATION, SYSTEM, SECURITY
     * @param operationType   operation type: CREATE, UPDATE, DELETE, etc.
     * @param operationTarget operation target: table or module name
     * @param beforeData      data before operation (JSON), nullable
     * @param afterData       data after operation (JSON), nullable
     * @param success         whether the operation succeeded
     * @param errorMsg        error message if the operation failed, nullable
     */
    void recordLog(String logType, String operationType, String operationTarget,
                   String beforeData, String afterData, boolean success, String errorMsg);

    /**
     * Export all logs matching the given query (without pagination) to CSV.
     *
     * <p>The CSV columns are: ID, LogType, OperatorName, OperationType,
     * OperationTarget, RequestMethod, RequestURL, IP, Status, CostTime,
     * CreateTime.</p>
     *
     * @param outputStream the output stream to write the CSV content to
     * @param query        the query parameters (filters only; page/size ignored)
     */
    void exportLogs(OutputStream outputStream, LogQueryDTO query);
}
