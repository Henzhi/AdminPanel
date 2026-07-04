package com.relic.admin.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Aliyun OSS file storage service.
 */
public interface OssService {

    /**
     * Upload a single file to OSS and return its public URL.
     */
    String uploadFile(MultipartFile file);

    /**
     * Upload multiple files to OSS and return their public URLs.
     */
    List<String> uploadFiles(MultipartFile[] files);

    /**
     * Delete a file from OSS by its public URL.
     */
    void deleteFile(String fileUrl);
}
