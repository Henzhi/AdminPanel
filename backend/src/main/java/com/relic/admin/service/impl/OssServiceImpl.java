package com.relic.admin.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.ResultCode;
import com.relic.admin.properties.OssProperties;
import com.relic.admin.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aliyun OSS implementation of {@link OssService}.
 *
 * <p>Generates a UUID-based object key for every upload and exposes the public
 * URL in the form {@code https://{bucket}.{endpoint}/{dir}/{filename}}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        String objectKey = buildObjectKey(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }
            PutObjectRequest request = new PutObjectRequest(
                    ossProperties.getBucketName(), objectKey, inputStream, metadata);
            ossClient.putObject(request);
        } catch (IOException e) {
            log.error("OSS upload IO failed: {}", originalFilename, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("OSS upload failed: {}", originalFilename, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败: " + e.getMessage());
        }

        return buildUrl(objectKey);
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        if (files == null || files.length == 0) {
            return urls;
        }
        for (MultipartFile file : files) {
            urls.add(uploadFile(file));
        }
        return urls;
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        String objectKey = extractObjectKey(fileUrl);
        if (objectKey == null || objectKey.isEmpty()) {
            return;
        }
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("OSS delete failed: {}", fileUrl, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件删除失败: " + e.getMessage());
        }
    }

    private String buildObjectKey(String fileName) {
        String dir = ossProperties.getDir();
        if (dir == null || dir.isEmpty()) {
            return fileName;
        }
        // normalize: trim leading/trailing slashes
        dir = dir.replaceAll("^/+|/+$", "");
        return dir + "/" + fileName;
    }

    private String buildUrl(String objectKey) {
        return "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + objectKey;
    }

    /**
     * Extract the OSS object key from a public URL of the form
     * {@code https://{bucket}.{endpoint}/{dir}/{filename}}.
     */
    private String extractObjectKey(String fileUrl) {
        int schemeEnd = fileUrl.indexOf("://");
        if (schemeEnd == -1) {
            return null;
        }
        int pathStart = fileUrl.indexOf("/", schemeEnd + 3);
        if (pathStart == -1) {
            return null;
        }
        return fileUrl.substring(pathStart + 1);
    }
}
