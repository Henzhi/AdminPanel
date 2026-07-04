package com.relic.admin.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.relic.admin.annotation.OperLog;
import com.relic.admin.common.AdminContextHolder;
import com.relic.admin.entity.SysLog;
import com.relic.admin.service.SysLogService;
import com.relic.admin.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AOP aspect that intercepts methods annotated with {@link OperLog} and
 * records an operation log entry.
 *
 * <p>Uses {@code @Around} advice so that both the success and exception paths
 * are captured. For each invocation the aspect records:</p>
 * <ul>
 *   <li>Request parameters (method arguments serialized to JSON, with
 *       sensitive fields such as passwords masked)</li>
 *   <li>The return value as {@code after_data} on success</li>
 *   <li>The exception message as {@code error_msg} on failure</li>
 *   <li>Execution cost time in milliseconds</li>
 *   <li>Operator identity from {@link AdminContextHolder}</li>
 *   <li>Request URL, HTTP method and client IP from the current HTTP request</li>
 * </ul>
 *
 * <p>Log persistence is delegated to {@link SysLogService#recordLog(SysLog)}
 * which executes asynchronously.</p>
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private final SysLogService sysLogService;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(SysLogService sysLogService, ObjectMapper objectMapper) {
        this.sysLogService = sysLogService;
        this.objectMapper = objectMapper;
    }

    /**
     * Intercept every method annotated with {@link OperLog}.
     *
     * @param joinPoint the proceeding join point
     * @param operLog   the annotation instance
     * @return the original method return value
     * @throws Throwable if the target method throws
     */
    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();

        // Capture request parameters before execution
        String requestParams = serializeArguments(method, joinPoint.getArgs());

        HttpServletRequest request = getCurrentRequest();

        Object result;
        boolean success = true;
        String errorMsg = null;
        String afterData = null;

        try {
            result = joinPoint.proceed();
            afterData = serializeValue(result);
        } catch (Throwable ex) {
            success = false;
            errorMsg = truncate(ex.getMessage(), 2000);
            throw ex;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            try {
                saveLog(operLog, methodName, requestParams, afterData, success, errorMsg,
                        costTime, request);
            } catch (Exception e) {
                log.error("Failed to record operation log for {}: {}", methodName, e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Build and asynchronously persist the log record.
     */
    private void saveLog(OperLog operLog, String methodName, String requestParams,
                         String afterData, boolean success, String errorMsg,
                         long costTime, HttpServletRequest request) {
        SysLog sysLog = new SysLog();
        sysLog.setLogType(operLog.logType());
        sysLog.setOperationType(operLog.operationType());
        sysLog.setOperationTarget(operLog.operationTarget());
        sysLog.setMethod(methodName);
        sysLog.setRequestParams(requestParams);
        sysLog.setAfterData(afterData);
        sysLog.setStatus(success ? 1 : 0);
        sysLog.setErrorMsg(errorMsg);
        sysLog.setCostTime(costTime);
        sysLog.setCreateTime(LocalDateTime.now());

        // Capture operator identity, tolerating the case where no user is
        // logged in (e.g. the login endpoint itself).
        try {
            if (AdminContextHolder.isLogin()) {
                sysLog.setOperatorId(AdminContextHolder.getCurrentAdminId());
                sysLog.setOperatorName(AdminContextHolder.getCurrentAdminName());
            } else {
                sysLog.setOperatorName("anonymous");
            }
        } catch (Exception e) {
            log.debug("Unable to resolve current admin in aspect: {}", e.getMessage());
            sysLog.setOperatorName("anonymous");
        }

        // Capture request metadata when inside an HTTP request.
        if (request != null) {
            sysLog.setRequestUrl(request.getRequestURI());
            sysLog.setRequestMethod(request.getMethod());
            sysLog.setIp(IpUtil.getIpAddress(request));
        }

        sysLogService.recordLog(sysLog);
    }

    /**
     * Serialize method arguments to a JSON string, excluding servlet objects,
     * file uploads and other non-serializable types. Sensitive fields whose
     * parameter name contains "password" are masked with {@code "***"}.
     */
    private String serializeArguments(Method method, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        Parameter[] parameters = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (isExcludedType(arg)) {
                continue;
            }

            String paramName = (i < parameters.length) ? parameters[i].getName() : "arg" + i;
            if (paramName.toLowerCase().contains("password")) {
                paramMap.put(paramName, "***");
            } else {
                paramMap.put(paramName, arg);
            }
        }

        if (paramMap.isEmpty()) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(paramMap);
            // Mask any password-like fields that may be nested inside DTOs.
            return maskSensitiveFields(json);
        } catch (Exception e) {
            log.debug("Failed to serialize method arguments: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Serialize a return value to JSON, excluding null and non-serializable types.
     */
    private String serializeValue(Object value) {
        if (value == null || isExcludedType(value)) {
            return null;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            return maskSensitiveFields(json);
        } catch (Exception e) {
            log.debug("Failed to serialize return value: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mask sensitive fields (password, token) in a JSON string by replacing
     * their values with {@code "***"}.
     */
    private String maskSensitiveFields(String json) {
        if (json == null) {
            return null;
        }
        // Matches "fieldName":"value" where fieldName contains password or token (case-insensitive)
        return json.replaceAll("(?i)(\"[^\"]*(?:password|token|secret)[^\"]*\"\\s*:\\s*)\"[^\"]*\"",
                "$1\"***\"");
    }

    /**
     * Determine whether an argument should be excluded from serialization.
     */
    private boolean isExcludedType(Object arg) {
        if (arg == null) {
            return true;
        }
        return arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof MultipartFile
                || arg instanceof byte[];
    }

    /**
     * Resolve the current HTTP request from the Spring request context.
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                return servletAttributes.getRequest();
            }
        } catch (Exception e) {
            log.debug("No request context available in aspect: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Truncate a string to the given maximum length.
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
