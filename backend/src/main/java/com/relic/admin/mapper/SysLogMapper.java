package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.relic.admin.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for {@link SysLog} with a custom paginated query that supports
 * multi-condition filtering by log type, operation type, operator name,
 * time range and keyword.
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    /**
     * Paginated log query with optional filters.
     *
     * @param page          pagination object (page number + size)
     * @param logType       log type filter, nullable
     * @param operationType operation type filter, nullable
     * @param operatorName  operator name filter (fuzzy), nullable
     * @param startDate     start of create time range, nullable
     * @param endDate       end of create time range, nullable
     * @param keyword       keyword to search operation_target / request_url, nullable
     * @return paginated result
     */
    IPage<SysLog> selectLogPage(IPage<SysLog> page,
                                @Param("logType") String logType,
                                @Param("operationType") String operationType,
                                @Param("operatorName") String operatorName,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate,
                                @Param("keyword") String keyword);

    /**
     * Select all logs matching the same filters without pagination.
     * Used for CSV export.
     *
     * @param logType       log type filter, nullable
     * @param operationType operation type filter, nullable
     * @param operatorName  operator name filter (fuzzy), nullable
     * @param startDate     start of create time range, nullable
     * @param endDate       end of create time range, nullable
     * @param keyword       keyword to search operation_target / request_url, nullable
     * @return list of matching logs
     */
    List<SysLog> selectLogList(@Param("logType") String logType,
                               @Param("operationType") String operationType,
                               @Param("operatorName") String operatorName,
                               @Param("startDate") String startDate,
                               @Param("endDate") String endDate,
                               @Param("keyword") String keyword);
}
