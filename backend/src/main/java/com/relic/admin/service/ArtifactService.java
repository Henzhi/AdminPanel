package com.relic.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.common.PageResult;
import com.relic.admin.dto.ArtifactDTO;
import com.relic.admin.dto.ArtifactQueryDTO;
import com.relic.admin.entity.Artifact;
import com.relic.admin.vo.ArtifactVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Artifact (文物) business service.
 */
public interface ArtifactService extends IService<Artifact> {

    /**
     * Paginated artifact query with optional filters.
     */
    PageResult<Artifact> getArtifactPage(ArtifactQueryDTO query);

    /**
     * Get artifact detail, parsing the images JSON into a list.
     */
    ArtifactVO getArtifactDetail(Long id);

    /**
     * Create a new artifact.
     */
    Artifact createArtifact(ArtifactDTO dto);

    /**
     * Update an existing artifact.
     */
    Artifact updateArtifact(Long id, ArtifactDTO dto);

    /**
     * Logically delete an artifact.
     */
    void deleteArtifact(Long id);

    /**
     * Import artifacts from a CSV stream. Returns the number of imported rows.
     */
    int importArtifacts(InputStream inputStream);

    /**
     * Export artifacts matching the query to a CSV stream.
     */
    void exportArtifacts(OutputStream outputStream, ArtifactQueryDTO query);

    /**
     * Upload a single image to OSS and return its public URL.
     */
    String uploadImage(MultipartFile file);

    /**
     * Upload multiple images to OSS and return their public URLs.
     */
    List<String> uploadImages(MultipartFile[] files);

    /**
     * Get distinct categories for the filter dropdown.
     */
    List<String> getCategories();
}
