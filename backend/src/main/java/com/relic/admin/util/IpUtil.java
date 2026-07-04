package com.relic.admin.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility for extracting the real client IP address from an HTTP request.
 *
 * <p>When the application is behind a reverse proxy (e.g. Nginx) the
 * {@link HttpServletRequest#getRemoteAddr()} returns the proxy IP rather than
 * the actual client. This utility inspects the standard forwarded headers
 * ({@code X-Forwarded-For}, {@code X-Real-IP}, etc.) and falls back to the
 * remote address when no proxy header is present.</p>
 */
public final class IpUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtil() {
    }

    /**
     * Resolve the real client IP address from the given request.
     *
     * <p>Checks the following headers in order: {@code X-Forwarded-For},
     * {@code X-Real-IP}, {@code Proxy-Client-IP}, {@code WL-Proxy-Client-IP}.
     * For {@code X-Forwarded-For} the first IP in the comma-separated list is
     * returned. Falls back to {@link HttpServletRequest#getRemoteAddr()} when
     * none of the headers contain a usable value.</p>
     *
     * @param request the HTTP servlet request
     * @return the client IP address, or {@code "unknown"} if it cannot be determined
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (isBlankOrUnknown(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For may contain a comma-separated chain: "client, proxy1, proxy2"
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // Normalize IPv6 loopback to IPv4 loopback for consistency
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }

        if (ip == null || ip.isEmpty()) {
            return UNKNOWN;
        }

        return ip;
    }

    private static boolean isBlankOrUnknown(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip.trim());
    }
}
