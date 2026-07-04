package com.relic.admin.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 前端动态菜单 VO
 */
@Data
public class MenuVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    /**
     * 菜单名称 (对应 sys_permission.perm_name)
     */
    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer sortOrder;

    private List<MenuVO> children = new ArrayList<>();
}
