package com.relic.admin.dto;

import lombok.Data;

/**
 * Paginated query parameters for the artifact list.
 */
@Data
public class ArtifactQueryDTO {

    private Integer page = 1;

    private Integer size = 10;

    /** Search keyword, matched against name and description. */
    private String keyword;

    private String era;

    private String category;

    private Integer status;
}
