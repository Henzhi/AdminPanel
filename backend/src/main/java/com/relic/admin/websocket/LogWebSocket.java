package com.relic.admin.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket endpoint for real-time log notifications to the admin dashboard.
 *
 * <p>Connected dashboard clients receive a broadcast message whenever a new
 * log record is persisted. The endpoint path is {@code /ws/logs}.</p>
 *
 * <p>Uses a {@link CopyOnWriteArraySet} to track active sessions, which is
 * safe for the low-volume, read-heavy broadcast pattern typical of admin
 * dashboards.</p>
 */
@Slf4j
@Component
@ServerEndpoint("/ws/logs")
public class LogWebSocket {

    /** Thread-safe set of all currently connected sessions. */
    private static final CopyOnWriteArraySet<Session> SESSIONS = new CopyOnWriteArraySet<>();

    /**
     * Called when a new client connects.
     *
     * @param session the WebSocket session
     */
    @OnOpen
    public void onOpen(Session session) {
        SESSIONS.add(session);
        log.info("WebSocket connected: {} (total: {})", session.getId(), SESSIONS.size());
    }

    /**
     * Called when a client disconnects.
     *
     * @param session the WebSocket session
     */
    @OnClose
    public void onClose(Session session) {
        SESSIONS.remove(session);
        log.info("WebSocket disconnected: {} (total: {})", session.getId(), SESSIONS.size());
    }

    /**
     * Called when a message is received from a client.
     *
     * <p>Currently this is a no-op aside from logging; the endpoint is
     * primarily used for server-to-client broadcast.</p>
     *
     * @param message the incoming message
     * @param session the WebSocket session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("WebSocket message from {}: {}", session.getId(), message);
    }

    /**
     * Called when an error occurs on a session.
     *
     * @param session the WebSocket session (may be null)
     * @param error   the error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        if (session != null) {
            SESSIONS.remove(session);
        }
        log.error("WebSocket error: {}", error.getMessage(), error);
    }

    /**
     * Broadcast a message to all connected dashboard clients.
     *
     * <p>Failed sends are logged and the offending session is removed so that
     * stale connections do not accumulate.</p>
     *
     * @param message the message to broadcast
     */
    public static void sendMessage(String message) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        for (Session session : SESSIONS) {
            if (session == null || !session.isOpen()) {
                SESSIONS.remove(session);
                continue;
            }
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.warn("Failed to send WebSocket message to {}: {}", session.getId(), e.getMessage());
                SESSIONS.remove(session);
            }
        }
    }
}
