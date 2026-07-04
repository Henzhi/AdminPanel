package com.relic.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.KnowledgeGraphDTO;
import com.relic.admin.entity.KnowledgeGraph;
import com.relic.admin.mapper.KnowledgeGraphMapper;
import com.relic.admin.service.KnowledgeGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link KnowledgeGraphService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphServiceImpl extends ServiceImpl<KnowledgeGraphMapper, KnowledgeGraph> implements KnowledgeGraphService {

    private final KnowledgeGraphMapper knowledgeGraphMapper;

    @Override
    public PageResult<KnowledgeGraph> getKnowledgeGraphPage(Integer page, Integer size, String keyword) {
        Page<KnowledgeGraph> p = new Page<>(page, size);
        LambdaQueryWrapper<KnowledgeGraph> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StrUtil.isNotBlank(keyword), w -> w
                .like(KnowledgeGraph::getSubjectEntity, keyword)
                .or().like(KnowledgeGraph::getRelation, keyword)
                .or().like(KnowledgeGraph::getObjectEntity, keyword));
        wrapper.orderByDesc(KnowledgeGraph::getCreateTime);
        IPage<KnowledgeGraph> result = knowledgeGraphMapper.selectPage(p, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public KnowledgeGraph getKnowledgeGraphDetail(Long id) {
        KnowledgeGraph kg = knowledgeGraphMapper.selectById(id);
        if (kg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识图谱记录不存在");
        }
        return kg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeGraph createKnowledgeGraph(KnowledgeGraphDTO dto) {
        KnowledgeGraph kg = new KnowledgeGraph();
        kg.setSubjectEntity(dto.getSubjectEntity());
        kg.setRelation(dto.getRelation());
        kg.setObjectEntity(dto.getObjectEntity());
        kg.setArtifactId(dto.getArtifactId());
        LocalDateTime now = LocalDateTime.now();
        kg.setCreateTime(now);
        kg.setUpdateTime(now);
        save(kg);
        return kg;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeGraph updateKnowledgeGraph(Long id, KnowledgeGraphDTO dto) {
        KnowledgeGraph kg = knowledgeGraphMapper.selectById(id);
        if (kg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识图谱记录不存在");
        }
        kg.setSubjectEntity(dto.getSubjectEntity());
        kg.setRelation(dto.getRelation());
        kg.setObjectEntity(dto.getObjectEntity());
        kg.setArtifactId(dto.getArtifactId());
        kg.setUpdateTime(LocalDateTime.now());
        updateById(kg);
        return kg;
    }

    @Override
    public void deleteKnowledgeGraph(Long id) {
        KnowledgeGraph kg = knowledgeGraphMapper.selectById(id);
        if (kg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "知识图谱记录不存在");
        }
        removeById(id);
    }

    @Override
    public void syncToGraphDatabase(Long id) {
        log.info("模拟同步知识图谱三元组到图数据库, record id={}", id);
    }
}
