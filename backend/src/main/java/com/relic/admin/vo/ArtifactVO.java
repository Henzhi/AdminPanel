package com.relic.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Artifact detail view object, exposing the parsed image list in addition to
 * the raw JSON string stored in the database.
 */
@Data
public class ArtifactVO {

    private Long id;

    private String name;

    private String era;

    private String category;

    private String description;

    private String imageUrl;

    private String images;

    private List<String> imageList;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
