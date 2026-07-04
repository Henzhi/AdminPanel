package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.relic.admin.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link BackupRecord}.
 */
@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {
}
