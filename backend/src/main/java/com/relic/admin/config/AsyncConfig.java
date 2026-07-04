package com.relic.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous task configuration.
 *
 * <p>Enables {@code @Async} support and defines a dedicated thread pool
 * ({@code logExecutor}) for log-recording tasks so that log persistence never
 * blocks the request thread.</p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool executor for asynchronous log recording.
     *
     * <p>Core pool size of 2, max pool size of 8, and a bounded queue of 500
     * tasks. When the queue is full the caller runs the task to apply
     * back-pressure rather than dropping logs.</p>
     *
     * @return the configured log executor
     */
    @Bean("logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
