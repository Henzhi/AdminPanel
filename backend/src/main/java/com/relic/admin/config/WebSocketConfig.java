package com.relic.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket configuration for admin notifications.
 *
 * <p>Registers {@link ServerEndpointExporter} so that {@code @ServerEndpoint}
 * annotated beans are discovered and exposed by the embedded servlet container.</p>
 */
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
