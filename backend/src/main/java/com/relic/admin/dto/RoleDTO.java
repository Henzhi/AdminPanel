package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色创建/更新 DTO
 *
 * <p>创建时 id 为空，更新时由控制器将路径参数 id 写入。</p>
 */
@Data
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键，更新时使用
     */
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    /**
     * 关联的权限ID列表
     */
    private List<Long> permissionIds;
}
