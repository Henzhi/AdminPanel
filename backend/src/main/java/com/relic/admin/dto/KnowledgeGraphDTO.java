package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for creating / updating a knowledge graph triple.
 */
@Data
public class KnowledgeGraphDTO {

    @NotBlank(message = "主体实体不能为空")
    private String subjectEntity;

    @NotBlank(message = "关系不能为空")
    private String relation;

    @NotBlank(message = "客体实体不能为空")
    private String objectEntity;

    private Long artifactId;
}
