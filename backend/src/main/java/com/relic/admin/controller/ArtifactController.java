package com.relic.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.Result;
import com.relic.admin.dto.ArtifactDTO;
import com.relic.admin.dto.ArtifactQueryDTO;
import com.relic.admin.entity.Artifact;
import com.relic.admin.service.ArtifactService;
import com.relic.admin.vo.ArtifactVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST controller for artifact (文物) management.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;

    @GetMapping
    public Result<PageResult<Artifact>> list(ArtifactQueryDTO query) {
        return Result.success(artifactService.getArtifactPage(query));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.success(artifactService.getCategories());
    }

    @GetMapping("/export")
    @SaCheckPermission("artifact:export")
    public void export(ArtifactQueryDTO query, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("artifacts.csv", StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        OutputStream outputStream = response.getOutputStream();
        artifactService.exportArtifacts(outputStream, query);
    }

    @GetMapping("/{id}")
    public Result<ArtifactVO> detail(@PathVariable Long id) {
        return Result.success(artifactService.getArtifactDetail(id));
    }

    @PostMapping
    @SaCheckPermission("artifact:create")
    public Result<Artifact> create(@Valid @RequestBody ArtifactDTO dto) {
        return Result.success(artifactService.createArtifact(dto));
    }

    @PostMapping("/import")
    @SaCheckPermission("artifact:import")
    public Result<Integer> importArtifacts(@RequestParam("file") MultipartFile file) throws IOException {
        int count = artifactService.importArtifacts(file.getInputStream());
        return Result.success("导入成功", count);
    }

    @PostMapping("/images")
    @SaCheckPermission("artifact:create")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(artifactService.uploadImage(file));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("artifact:update")
    public Result<Artifact> update(@PathVariable Long id, @Valid @RequestBody ArtifactDTO dto) {
        return Result.success(artifactService.updateArtifact(id, dto));
    }

    @PostMapping("/{id}/images")
    @SaCheckPermission("artifact:update")
    public Result<List<String>> uploadImages(@PathVariable Long id, @RequestParam("files") MultipartFile[] files) {
        return Result.success(artifactService.uploadImages(files));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("artifact:delete")
    public Result<Void> delete(@PathVariable Long id) {
        artifactService.deleteArtifact(id);
        return Result.success();
    }
}
