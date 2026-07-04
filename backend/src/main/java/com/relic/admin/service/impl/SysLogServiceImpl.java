package com.relic.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opencsv.CSVWriter;
import com.relic.admin.common.AdminContextHolder;
import com.relic.admin.common.PageResult;
import com.relic.admin.dto.LogQueryDTO;
import com.relic.admin.entity.SysLog;
import com.relic.admin.mapper.SysLogMapper;
import com.relic.admin.service.SysLogService;
import com.relic.admin.util.IpUtil;
import com.relic.admin.websocket.LogWebSocket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of {@link SysLogService}.
 *
 * <p>Log persistence is performed asynchronously via the {@code logExecutor}
 * thread pool (see {@link com.relic.admin.config.AsyncConfig}) so that audit
 * logging never blocks the business request. Real-time notifications are
 * broadcast to connected dashboard clients through {@link LogWebSocket}.</p>
 */
@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private static final DateTimeFormatter CSV_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] CSV_HEADERS = {
            "ID", "LogType", "OperatorName", "OperationType", "OperationTarget",
            "RequestMethod", "RequestURL", "IP", "Status", "CostTime", "CreateTime"
    };

    @Override
    public PageResult<SysLog> getLogPage(LogQueryDTO query) {
        int pageNum = (query.getPage() == null || query.getPage() < 1) ? 1 : query.getPage();
        int pageSize = (query.getSize() == null || query.getSize() < 1) ? 10 : query.getSize();

        Page<SysLog> page = new Page<>(pageNum, pageSize);
        IPage<SysLog> result = baseMapper.selectLogPage(
                page,
                query.getLogType(),
                query.getOperationType(),
                query.getOperatorName(),
                query.getStartDate(),
                query.getEndDate(),
                query.getKeyword());

        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Async("logExecutor")
    @Override
    public void recordLog(SysLog sysLog) {
        if (sysLog == null) {
            return;
        }
        if (sysLog.getCreateTime() == null) {
            sysLog.setCreateTime(LocalDateTime.now());
        }
        save(sysLog);
        notifyWebSocket(sysLog);
    }

    @Async("logExecutor")
    @Override
    public void recordLog(String logType, String operationType, String operationTarget,
                          String beforeData, String afterData, boolean success, String errorMsg) {
        SysLog sysLog = new SysLog();
        sysLog.setLogType(logType);
        sysLog.setOperationType(operationType);
        sysLog.setOperationTarget(operationTarget);
        sysLog.setBeforeData(beforeData);
        sysLog.setAfterData(afterData);
        sysLog.setStatus(success ? 1 : 0);
        if (!success && errorMsg != null) {
            sysLog.setErrorMsg(errorMsg);
        }
        sysLog.setCostTime(0L);
        sysLog.setCreateTime(LocalDateTime.now());

        // Capture operator info, tolerating the case where no user is logged in
        // (e.g. a failed login attempt before authentication succeeds).
        try {
            if (AdminContextHolder.isLogin()) {
                sysLog.setOperatorId(AdminContextHolder.getCurrentAdminId());
                sysLog.setOperatorName(AdminContextHolder.getCurrentAdminName());
            } else {
                sysLog.setOperatorName("anonymous");
            }
        } catch (Exception e) {
            log.debug("Unable to resolve current admin for log recording: {}", e.getMessage());
            sysLog.setOperatorName("anonymous");
        }

        // Capture request info when running inside an HTTP request thread.
        // For non-HTTP contexts (e.g. scheduled tasks) these stay null.
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            sysLog.setRequestUrl(request.getRequestURI());
            sysLog.setRequestMethod(request.getMethod());
            sysLog.setIp(IpUtil.getIpAddress(request));
        }

        save(sysLog);
        notifyWebSocket(sysLog);
    }

    @Override
    public void exportLogs(OutputStream outputStream, LogQueryDTO query) {
        List<SysLog> logs = baseMapper.selectLogList(
                query.getLogType(),
                query.getOperationType(),
                query.getOperatorName(),
                query.getStartDate(),
                query.getEndDate(),
                query.getKeyword());

        try (CSVWriter csvWriter = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            csvWriter.writeNext(CSV_HEADERS);
            for (SysLog logEntry : logs) {
                csvWriter.writeNext(buildCsvRow(logEntry));
            }
        } catch (Exception e) {
            log.error("Failed to export logs to CSV", e);
            throw new RuntimeException("Failed to export logs to CSV", e);
        }
    }

    /**
     * Resolve the current {@link HttpServletRequest} from the Spring request
     * context, or {@code null} when not running inside an HTTP request thread.
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                return servletAttributes.getRequest();
            }
        } catch (Exception e) {
            log.debug("No request context available: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Build a single CSV row from a log record.
     */
    private String[] buildCsvRow(SysLog logEntry) {
        return new String[]{
                String.valueOf(logEntry.getId()),
                nullToEmpty(logEntry.getLogType()),
                nullToEmpty(logEntry.getOperatorName()),
                nullToEmpty(logEntry.getOperationType()),
                nullToEmpty(logEntry.getOperationTarget()),
                nullToEmpty(logEntry.getRequestMethod()),
                nullToEmpty(logEntry.getRequestUrl()),
                nullToEmpty(logEntry.getIp()),
                logEntry.getStatus() != null ? String.valueOf(logEntry.getStatus()) : "",
                logEntry.getCostTime() != null ? String.valueOf(logEntry.getCostTime()) : "",
                logEntry.getCreateTime() != null ? logEntry.getCreateTime().format(CSV_DATE_FORMATTER) : ""
        };
    }

    /**
     * Broadcast a log notification to connected WebSocket dashboard clients.
     * Failures are swallowed so they never affect log persistence.
     */
    private void notifyWebSocket(SysLog sysLog) {
        try {
            String message = String.format("[%s] %s | %s | %s | %s",
                    sysLog.getLogType(),
                    sysLog.getOperationType(),
                    nullToEmpty(sysLog.getOperatorName()),
                    nullToEmpty(sysLog.getOperationTarget()),
                    sysLog.getStatus() != null && sysLog.getStatus() == 1 ? "SUCCESS" : "FAILED");
            LogWebSocket.sendMessage(message);
        } catch (Exception e) {
            log.debug("WebSocket notification skipped: {}", e.getMessage());
        }
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
