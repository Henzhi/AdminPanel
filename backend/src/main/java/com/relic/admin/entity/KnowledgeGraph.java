package com.relic.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Knowledge graph triple (主体-关系-客体) mapped to the {@code knowledge_graph} table.
 */
@Data
@TableName("knowledge_graph")
public class KnowledgeGraph {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String subjectEntity;

    private String relation;

    private String objectEntity;

    private Long artifactId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
