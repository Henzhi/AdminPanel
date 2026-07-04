package com.relic.admin.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.relic.admin.properties.BackupProperties;
import com.relic.admin.properties.OssProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Aliyun OSS client configuration.
 *
 * <p>Builds an {@link OSS} client bean from {@link OssProperties} and enables
 * binding of both {@code oss} and {@code backup} configuration properties.</p>
 */
@Configuration
@EnableConfigurationProperties({OssProperties.class, BackupProperties.class})
public class OssConfig {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties ossProperties) {
        return new OSSClientBuilder()
                .build(ossProperties.getEndpoint(),
                        ossProperties.getAccessKeyId(),
                        ossProperties.getAccessKeySecret());
    }
}
