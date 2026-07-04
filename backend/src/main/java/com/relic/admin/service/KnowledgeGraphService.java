package com.relic.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.relic.admin.common.PageResult;
import com.relic.admin.dto.KnowledgeGraphDTO;
import com.relic.admin.entity.KnowledgeGraph;

/**
 * Knowledge graph triple business service.
 */
public interface KnowledgeGraphService extends IService<KnowledgeGraph> {

    /**
     * Paginated knowledge graph query with an optional keyword.
     */
    PageResult<KnowledgeGraph> getKnowledgeGraphPage(Integer page, Integer size, String keyword);

    /**
     * Get a knowledge graph triple by id.
     */
    KnowledgeGraph getKnowledgeGraphDetail(Long id);

    /**
     * Create a new knowledge graph triple.
     */
    KnowledgeGraph createKnowledgeGraph(KnowledgeGraphDTO dto);

    /**
     * Update an existing knowledge graph triple.
     */
    KnowledgeGraph updateKnowledgeGraph(Long id, KnowledgeGraphDTO dto);

    /**
     * Logically delete a knowledge graph triple.
     */
    void deleteKnowledgeGraph(Long id);

    /**
     * Simulate syncing a triple to the graph database.
     */
    void syncToGraphDatabase(Long id);
}
