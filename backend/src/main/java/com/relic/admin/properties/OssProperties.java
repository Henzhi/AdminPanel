package com.relic.admin.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Aliyun OSS configuration properties bound to the {@code oss} prefix.
 */
@Data
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** OSS endpoint, e.g. oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    /** Access key id from Aliyun RAM */
    private String accessKeyId;

    /** Access key secret from Aliyun RAM */
    private String accessKeySecret;

    /** Bucket name for file storage */
    private String bucketName;

    /** Directory prefix inside the bucket */
    private String dir;
}
