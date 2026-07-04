package com.relic.admin.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    NO_CONTENT(204, "无内容"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无操作权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不被允许"),
    CONFLICT(409, "资源冲突"),

    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // 业务错误码 1xxxx
    USERNAME_OR_PASSWORD_ERROR(10001, "用户名或密码错误"),
    ACCOUNT_DISABLED(10002, "账号已被禁用"),
    ACCOUNT_NOT_FOUND(10003, "账号不存在"),
    USERNAME_EXISTS(10004, "用户名已存在"),
    OLD_PASSWORD_ERROR(10005, "原密码错误"),
    CAPTCHA_ERROR(10006, "验证码错误"),
    PERMISSION_DENIED(10007, "权限不足"),

    ARTIFACT_NOT_FOUND(20001, "文物不存在"),
    BACKUP_FAILED(30001, "备份失败"),
    RESTORE_FAILED(30002, "恢复失败"),
    BACKUP_FILE_NOT_FOUND(30003, "备份文件不存在"),
    RESTORE_CONFIRM_ERROR(30004, "恢复确认密码错误"),
    SYSTEM_MAINTAINING(30005, "系统维护中，请稍后操作");

    private final Integer code;
    private final String message;
}
