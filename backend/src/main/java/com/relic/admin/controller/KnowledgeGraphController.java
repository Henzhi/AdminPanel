package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.dto.KnowledgeGraphDTO;
import com.relic.admin.entity.KnowledgeGraph;
import com.relic.admin.service.KnowledgeGraphService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for knowledge graph triple management.
 */
@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
public class KnowledgeGraphController {

    private final KnowledgeGraphService knowledgeGraphService;

    @GetMapping
    public Result<PageResult<KnowledgeGraph>> list(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam(required = false) String keyword) {
        return Result.success(knowledgeGraphService.getKnowledgeGraphPage(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<KnowledgeGraph> detail(@PathVariable Long id) {
        return Result.success(knowledgeGraphService.getKnowledgeGraphDetail(id));
    }

    @PostMapping
    @SaCheckPermission("knowledge:create")
    public Result<KnowledgeGraph> create(@Valid @RequestBody KnowledgeGraphDTO dto) {
        return Result.success(knowledgeGraphService.createKnowledgeGraph(dto));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("knowledge:update")
    public Result<KnowledgeGraph> update(@PathVariable Long id, @Valid @RequestBody KnowledgeGraphDTO dto) {
        return Result.success(knowledgeGraphService.updateKnowledgeGraph(id, dto));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("knowledge:delete")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeGraphService.deleteKnowledgeGraph(id);
        return Result.success();
    }

    @PostMapping("/{id}/sync")
    public Result<Void> sync(@PathVariable Long id) {
        knowledgeGraphService.syncToGraphDatabase(id);
        return Result.success();
    }
}
