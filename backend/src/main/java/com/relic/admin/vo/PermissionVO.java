package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限展示 VO (支持树形结构)
 */
@Data
public class PermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String permCode;

    private String permName;

    private Integer permType;

    private String path;

    private String component;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private List<PermissionVO> children = new ArrayList<>();
}
