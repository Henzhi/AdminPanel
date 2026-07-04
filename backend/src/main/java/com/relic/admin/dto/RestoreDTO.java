package com.relic.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * Request body for restoring a backup.
 *
 * <p>The {@code confirmPassword} acts as a 2FA confirmation: the caller must
 * re-enter their own login password before a destructive restore is allowed.</p>
 */
@Data
public class RestoreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID of the backup record to restore */
    @NotNull(message = "备份ID不能为空")
    private Long backupId;

    /** Current admin password confirmation */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
