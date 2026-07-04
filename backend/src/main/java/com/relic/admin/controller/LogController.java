package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.relic.admin.common.Constants;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.LogQueryDTO;
import com.relic.admin.entity.SysLog;
import com.relic.admin.service.SysLogService;
import com.relic.admin.vo.SysLogVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for log management.
 *
 * <p>Exposes endpoints for querying operation, security and system logs,
 * viewing log detail, exporting filtered results to CSV, and deleting log
 * records (restricted to super administrators).</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/logs")
public class LogController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SysLogService sysLogService;

    public LogController(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    /**
     * Paginated log list with all query filters.
     *
     * @param query query parameters (page, size, logType, operationType,
     *              operatorName, startDate, endDate, keyword)
     * @return paginated log records
     */
    @GetMapping
    public Result<PageResult<SysLogVO>> list(@ModelAttribute LogQueryDTO query) {
        PageResult<SysLog> pageResult = sysLogService.getLogPage(query);
        List<SysLogVO> voList = pageResult.getList().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(pageResult.getTotal(), voList));
    }

    /**
     * Paginated operation logs only.
     *
     * @param query query parameters (page, size, filters except logType)
     * @return paginated operation log records
     */
    @GetMapping("/operation")
    public Result<PageResult<SysLogVO>> operationLogs(@ModelAttribute LogQueryDTO query) {
        query.setLogType(Constants.LOG_TYPE_OPERATION);
        return list(query);
    }

    /**
     * Paginated security logs only.
     *
     * @param query query parameters (page, size, filters except logType)
     * @return paginated security log records
     */
    @GetMapping("/security")
    public Result<PageResult<SysLogVO>> securityLogs(@ModelAttribute LogQueryDTO query) {
        query.setLogType(Constants.LOG_TYPE_SECURITY);
        return list(query);
    }

    /**
     * Paginated system logs only.
     *
     * @param query query parameters (page, size, filters except logType)
     * @return paginated system log records
     */
    @GetMapping("/system")
    public Result<PageResult<SysLogVO>> systemLogs(@ModelAttribute LogQueryDTO query) {
        query.setLogType(Constants.LOG_TYPE_SYSTEM);
        return list(query);
    }

    /**
     * Get a single log record by ID.
     *
     * @param id the log record ID
     * @return the log detail
     */
    @GetMapping("/{id}")
    public Result<SysLogVO> detail(@PathVariable Long id) {
        SysLog sysLog = sysLogService.getById(id);
        if (sysLog == null) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success(toVO(sysLog));
    }

    /**
     * Export filtered logs to CSV.
     *
     * @param query    query parameters (filters only; page/size ignored)
     * @param response the HTTP response to write the CSV content to
     */
    @GetMapping("/export")
    public void export(@ModelAttribute LogQueryDTO query, HttpServletResponse response) {
        try {
            String filename = URLEncoder.encode("logs_" + LocalDateTime.now().format(FORMATTER) + ".csv",
                    StandardCharsets.UTF_8);
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            // Write UTF-8 BOM so Excel correctly detects encoding
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            sysLogService.exportLogs(outputStream, query);
            outputStream.flush();
        } catch (Exception e) {
            log.error("Failed to export logs", e);
            throw new RuntimeException("Failed to export logs", e);
        }
    }

    /**
     * Delete a log record. Restricted to super administrators.
     *
     * @param id the log record ID
     * @return success result
     */
    @SaCheckRole("SUPER_ADMIN")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean removed = sysLogService.removeById(id);
        if (!removed) {
            return Result.error(ResultCode.NOT_FOUND);
        }
        return Result.success();
    }

    /**
     * Convert a {@link SysLog} entity to a {@link SysLogVO}, populating the
     * formatted create time string.
     */
    private SysLogVO toVO(SysLog sysLog) {
        SysLogVO vo = new SysLogVO();
        vo.setId(sysLog.getId());
        vo.setLogType(sysLog.getLogType());
        vo.setOperatorId(sysLog.getOperatorId());
        vo.setOperatorName(sysLog.getOperatorName());
        vo.setOperationType(sysLog.getOperationType());
        vo.setOperationTarget(sysLog.getOperationTarget());
        vo.setMethod(sysLog.getMethod());
        vo.setRequestUrl(sysLog.getRequestUrl());
        vo.setRequestMethod(sysLog.getRequestMethod());
        vo.setRequestParams(sysLog.getRequestParams());
        vo.setBeforeData(sysLog.getBeforeData());
        vo.setAfterData(sysLog.getAfterData());
        vo.setIp(sysLog.getIp());
        vo.setStatus(sysLog.getStatus());
        vo.setErrorMsg(sysLog.getErrorMsg());
        vo.setCostTime(sysLog.getCostTime());
        vo.setCreateTime(sysLog.getCreateTime());
        if (sysLog.getCreateTime() != null) {
            vo.setFormattedCreateTime(sysLog.getCreateTime().format(FORMATTER));
        }
        return vo;
    }
}
