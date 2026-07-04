package com.relic.admin.service;

import com.relic.admin.common.PageResult;
import com.relic.admin.dto.BackupQueryDTO;
import com.relic.admin.dto.RestoreDTO;
import com.relic.admin.entity.BackupRecord;
import com.relic.admin.vo.BackupRecordVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Backup &amp; recovery service.
 *
 * <p>Handles manual/scheduled database backups (optionally AES-encrypted),
 * restoring from a backup file with 2FA confirmation, downloading backup
 * files and querying backup history.</p>
 */
public interface BackupService {

    /**
     * Paginated query of backup records with optional filters.
     *
     * @param query filter and pagination parameters
     * @return paginated backup record view objects
     */
    PageResult<BackupRecordVO> getBackupPage(BackupQueryDTO query);

    /**
     * Trigger a manual full backup.
     *
     * @param encrypt whether the resulting file should be AES-encrypted
     * @return the persisted backup record
     */
    BackupRecord createBackup(boolean encrypt);

    /**
     * Restore the database from a backup file.
     *
     * <p>Requires SUPER_ADMIN role and a valid password confirmation. The
     * system is put into maintenance mode for the duration of the restore.</p>
     *
     * @param dto restore request containing backup id and password confirmation
     */
    void restoreBackup(RestoreDTO dto);

    /**
     * Stream a backup file to the HTTP response for download.
     *
     * @param id       backup record id
     * @param response HTTP response to write the file to
     */
    void downloadBackup(Long id, HttpServletResponse response);

    /**
     * Get the detail of a single backup record.
     *
     * @param id backup record id
     * @return backup record view object
     */
    BackupRecordVO getBackupDetail(Long id);
}
