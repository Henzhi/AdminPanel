package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request body for creating / updating an artifact.
 */
@Data
public class ArtifactDTO {

    @NotBlank(message = "文物名称不能为空")
    private String name;

    private String era;

    private String category;

    private String description;

    private String imageUrl;

    private List<String> images;

    private Integer status;
}
