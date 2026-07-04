package com.relic.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.relic.admin.entity.Artifact;
import org.apache.ibatis.annotations.Param;

/**
 * MyBatis-Plus mapper for {@link Artifact}.
 */
public interface ArtifactMapper extends BaseMapper<Artifact> {

    /**
     * Paginated artifact query with optional filters.
     *
     * @param page     pagination object
     * @param keyword  search keyword (name / description)
     * @param era      era filter
     * @param category category filter
     * @param status   status filter
     * @return paginated artifact list
     */
    IPage<Artifact> selectArtifactPage(Page<Artifact> page,
                                       @Param("keyword") String keyword,
                                       @Param("era") String era,
                                       @Param("category") String category,
                                       @Param("status") Integer status);
}
