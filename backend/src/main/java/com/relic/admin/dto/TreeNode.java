package com.relic.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用树节点，用于权限树等场景
 *
 * @param <T> 节点数据类型
 */
@Data
public class TreeNode<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String label;

    private List<TreeNode<T>> children = new ArrayList<>();

    /**
     * 添加子节点
     */
    public void addChild(TreeNode<T> child) {
        this.children.add(child);
    }
}
