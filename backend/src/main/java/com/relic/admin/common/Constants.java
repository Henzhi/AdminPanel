package com.relic.admin.common;

/**
 * 系统常量
 */
public class Constants {

    private Constants() {
    }

    /** 管理员状态 - 正常 */
    public static final int ADMIN_STATUS_ACTIVE = 1;
    /** 管理员状态 - 禁用 */
    public static final int ADMIN_STATUS_DISABLED = 0;

    /** 权限类型 - 菜单 */
    public static final int PERM_TYPE_MENU = 1;
    /** 权限类型 - 按钮 */
    public static final int PERM_TYPE_BUTTON = 2;

    /** 日志类型 - 操作日志 */
    public static final String LOG_TYPE_OPERATION = "OPERATION";
    /** 日志类型 - 系统日志 */
    public static final String LOG_TYPE_SYSTEM = "SYSTEM";
    /** 日志类型 - 安全日志 */
    public static final String LOG_TYPE_SECURITY = "SECURITY";

    /** 操作类型 */
    public static final String OP_CREATE = "CREATE";
    public static final String OP_UPDATE = "UPDATE";
    public static final String OP_DELETE = "DELETE";
    public static final String OP_LOGIN = "LOGIN";
    public static final String OP_LOGOUT = "LOGOUT";
    public static final String OP_BACKUP = "BACKUP";
    public static final String OP_RESTORE = "RESTORE";
    public static final String OP_IMPORT = "IMPORT";
    public static final String OP_EXPORT = "EXPORT";
    public static final String OP_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String OP_PERMISSION_CHANGE = "PERMISSION_CHANGE";

    /** 备份类型 - 全量 */
    public static final String BACKUP_TYPE_FULL = "FULL";

    /** 系统维护状态 Redis Key */
    public static final String SYSTEM_MAINTAINING_KEY = "system:maintaining";

    /** Sa-Token 管理员会话前缀 */
    public static final String SA_ADMIN_PREFIX = "admin:";
}
