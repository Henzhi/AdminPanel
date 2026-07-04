package com.relic.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cultural Heritage Admin Panel Application Entry Point.
 */
@SpringBootApplication
@MapperScan("com.relic.admin.mapper")
@EnableScheduling
public class RelicAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelicAdminApplication.class, args);
    }
}
