package com.relic.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.relic.admin.common.BusinessException;
import com.relic.admin.common.PageResult;
import com.relic.admin.common.ResultCode;
import com.relic.admin.dto.ArtifactDTO;
import com.relic.admin.dto.ArtifactQueryDTO;
import com.relic.admin.entity.Artifact;
import com.relic.admin.mapper.ArtifactMapper;
import com.relic.admin.service.ArtifactService;
import com.relic.admin.service.OssService;
import com.relic.admin.vo.ArtifactVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ArtifactService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArtifactServiceImpl extends ServiceImpl<ArtifactMapper, Artifact> implements ArtifactService {

    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ArtifactMapper artifactMapper;
    private final OssService ossService;

    @Override
    public PageResult<Artifact> getArtifactPage(ArtifactQueryDTO query) {
        Page<Artifact> page = new Page<>(query.getPage(), query.getSize());
        IPage<Artifact> result = artifactMapper.selectArtifactPage(
                page, query.getKeyword(), query.getEra(), query.getCategory(), query.getStatus());
        return new PageResult<>(result.getTotal(), result.getRecords());
    }

    @Override
    public ArtifactVO getArtifactDetail(Long id) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new BusinessException(ResultCode.ARTIFACT_NOT_FOUND);
        }
        ArtifactVO vo = new ArtifactVO();
        vo.setId(artifact.getId());
        vo.setName(artifact.getName());
        vo.setEra(artifact.getEra());
        vo.setCategory(artifact.getCategory());
        vo.setDescription(artifact.getDescription());
        vo.setImageUrl(artifact.getImageUrl());
        vo.setImages(artifact.getImages());
        vo.setStatus(artifact.getStatus());
        vo.setCreateTime(artifact.getCreateTime());
        vo.setUpdateTime(artifact.getUpdateTime());
        vo.setImageList(parseImages(artifact.getImages()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Artifact createArtifact(ArtifactDTO dto) {
        Artifact artifact = new Artifact();
        artifact.setName(dto.getName());
        artifact.setEra(dto.getEra());
        artifact.setCategory(dto.getCategory());
        artifact.setDescription(dto.getDescription());
        artifact.setImageUrl(dto.getImageUrl());
        artifact.setImages(joinImages(dto.getImages()));
        artifact.setStatus(dto.getStatus());
        LocalDateTime now = LocalDateTime.now();
        artifact.setCreateTime(now);
        artifact.setUpdateTime(now);
        save(artifact);
        return artifact;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Artifact updateArtifact(Long id, ArtifactDTO dto) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new BusinessException(ResultCode.ARTIFACT_NOT_FOUND);
        }
        artifact.setName(dto.getName());
        artifact.setEra(dto.getEra());
        artifact.setCategory(dto.getCategory());
        artifact.setDescription(dto.getDescription());
        artifact.setImageUrl(dto.getImageUrl());
        artifact.setImages(joinImages(dto.getImages()));
        artifact.setStatus(dto.getStatus());
        artifact.setUpdateTime(LocalDateTime.now());
        updateById(artifact);
        return artifact;
    }

    @Override
    public void deleteArtifact(Long id) {
        Artifact artifact = artifactMapper.selectById(id);
        if (artifact == null) {
            throw new BusinessException(ResultCode.ARTIFACT_NOT_FOUND);
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importArtifacts(InputStream inputStream) {
        List<Artifact> artifacts = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                if (row.length < 4) {
                    log.warn("跳过列数不足的CSV行: {}", List.of(row));
                    continue;
                }
                Artifact artifact = new Artifact();
                artifact.setName(StrUtil.trimToNull(row[0]));
                artifact.setEra(StrUtil.trimToNull(row[1]));
                artifact.setCategory(StrUtil.trimToNull(row[2]));
                artifact.setDescription(row[3]);
                artifact.setStatus(1);
                LocalDateTime now = LocalDateTime.now();
                artifact.setCreateTime(now);
                artifact.setUpdateTime(now);
                artifacts.add(artifact);
            }
        } catch (IOException | CsvException e) {
            log.error("CSV导入解析失败", e);
            throw new BusinessException(ResultCode.BAD_REQUEST, "CSV导入失败: " + e.getMessage());
        }

        if (!artifacts.isEmpty()) {
            saveBatch(artifacts);
        }
        log.info("CSV导入完成, 共 {} 条文物记录", artifacts.size());
        return artifacts.size();
    }

    @Override
    public void exportArtifacts(OutputStream outputStream, ArtifactQueryDTO query) {
        // Query all matching records without pagination.
        Page<Artifact> page = new Page<>(1, Integer.MAX_VALUE);
        page.setSearchCount(false);
        IPage<Artifact> result = artifactMapper.selectArtifactPage(
                page, query.getKeyword(), query.getEra(), query.getCategory(), query.getStatus());
        List<Artifact> list = result.getRecords();

        try {
            // UTF-8 BOM so Excel correctly renders Chinese characters.
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        } catch (IOException e) {
            log.warn("写入CSV BOM失败", e);
        }

        try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw)) {
            writer.writeNext(new String[]{"ID", "Name", "Era", "Category", "Description", "ImageURL", "Status", "CreateTime"});
            for (Artifact a : list) {
                writer.writeNext(new String[]{
                        a.getId() == null ? "" : String.valueOf(a.getId()),
                        a.getName(),
                        a.getEra(),
                        a.getCategory(),
                        a.getDescription(),
                        a.getImageUrl(),
                        a.getStatus() == null ? "" : String.valueOf(a.getStatus()),
                        a.getCreateTime() == null ? "" : a.getCreateTime().format(CSV_DATE_FORMATTER)
                });
            }
        } catch (IOException e) {
            log.error("CSV导出失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "CSV导出失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return ossService.uploadFile(file);
    }

    @Override
    public List<String> uploadImages(MultipartFile[] files) {
        return ossService.uploadFiles(files);
    }

    @Override
    public List<String> getCategories() {
        QueryWrapper<Artifact> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT category");
        wrapper.isNotNull("category").ne("category", "");
        List<Object> objs = artifactMapper.selectObjs(wrapper);
        return objs.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * Serialize the image URL list to a JSON array string.
     */
    private String joinImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return JSONUtil.toJsonStr(images);
    }

    /**
     * Parse the images JSON array string into a list, tolerating null / blank values.
     */
    private List<String> parseImages(String images) {
        if (StrUtil.isBlank(images)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(images, String.class);
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", images, e);
            return new ArrayList<>();
        }
    }
}
