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
 * Cultural relic (文物) entity mapped to the {@code artifact} table.
 */
@Data
@TableName("artifact")
public class Artifact {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String era;

    private String category;

    private String description;

    private String imageUrl;

    /** Multiple image URLs stored as a JSON array string. */
    private String images;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
