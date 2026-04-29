package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryContentBlockUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryContentBlockResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.ContentAssetLink;
import com.aoxiaoyou.admin.entity.StoryChapterBlockLink;
import com.aoxiaoyou.admin.entity.StoryContentBlock;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterBlockLinkMapper;
import com.aoxiaoyou.admin.mapper.StoryContentBlockMapper;
import com.aoxiaoyou.admin.service.AdminStoryContentBlockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminStoryContentBlockServiceImpl implements AdminStoryContentBlockService {

    private final StoryContentBlockMapper storyContentBlockMapper;
    private final StoryChapterBlockLinkMapper storyChapterBlockLinkMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final ContentAssetLinkMapper contentAssetLinkMapper;

    @Override
    public PageResponse<AdminStoryContentBlockResponse> page(long pageNum, long pageSize, String keyword, String blockType, String status) {
        Page<StoryContentBlock> page = storyContentBlockMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryContentBlock>()
                        .eq(StringUtils.hasText(blockType), StoryContentBlock::getBlockType, blockType)
                        .eq(StringUtils.hasText(status), StoryContentBlock::getStatus, status)
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(StoryContentBlock::getCode, keyword)
                                .or().like(StoryContentBlock::getTitleZh, keyword)
                                .or().like(StoryContentBlock::getTitleZht, keyword)
                                .or().like(StoryContentBlock::getTitleEn, keyword)
                                .or().like(StoryContentBlock::getTitlePt, keyword))
                        .orderByAsc(StoryContentBlock::getSortOrder)
                        .orderByAsc(StoryContentBlock::getId));
        Page<AdminStoryContentBlockResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminStoryContentBlockResponse detail(Long blockId) {
        return toResponse(requireBlock(blockId));
    }

    @Override
    public AdminStoryContentBlockResponse create(AdminStoryContentBlockUpsertRequest request) {
        StoryContentBlock block = new StoryContentBlock();
        applyRequest(block, request);
        storyContentBlockMapper.insert(block);
        syncAttachmentLinks(block.getId(), request.getAttachmentAssetIds(), block.getBlockType(), block.getStatus());
        return toResponse(requireBlock(block.getId()));
    }

    @Override
    public AdminStoryContentBlockResponse update(Long blockId, AdminStoryContentBlockUpsertRequest request) {
        StoryContentBlock block = requireBlock(blockId);
        applyRequest(block, request);
        storyContentBlockMapper.updateById(block);
        syncAttachmentLinks(blockId, request.getAttachmentAssetIds(), block.getBlockType(), block.getStatus());
        return toResponse(requireBlock(blockId));
    }

    @Override
    public void delete(Long blockId) {
        requireBlock(blockId);
        Long usageCount = storyChapterBlockLinkMapper.selectCount(new LambdaQueryWrapper<StoryChapterBlockLink>()
                .eq(StoryChapterBlockLink::getBlockId, blockId));
        if (usageCount != null && usageCount > 0) {
            throw new BusinessException(4057, "Story content block is still linked to chapters");
        }
        contentAssetLinkMapper.delete(new LambdaQueryWrapper<ContentAssetLink>()
                .eq(ContentAssetLink::getEntityType, "story_content_block")
                .eq(ContentAssetLink::getEntityId, blockId));
        storyContentBlockMapper.deleteById(blockId);
    }

    private void applyRequest(StoryContentBlock block, AdminStoryContentBlockUpsertRequest request) {
        block.setCode(resolveCode(request.getCode(), request.getBlockType(), block.getId()));
        block.setBlockType(normalizeBlockType(request.getBlockType()));
        block.setTitleZh(trimToNull(request.getTitleZh()));
        block.setTitleEn(trimToNull(request.getTitleEn()));
        block.setTitleZht(trimToNull(request.getTitleZht()));
        block.setTitlePt(trimToNull(request.getTitlePt()));
        block.setSummaryZh(trimToNull(request.getSummaryZh()));
        block.setSummaryEn(trimToNull(request.getSummaryEn()));
        block.setSummaryZht(trimToNull(request.getSummaryZht()));
        block.setSummaryPt(trimToNull(request.getSummaryPt()));
        block.setBodyZh(trimToNull(request.getBodyZh()));
        block.setBodyEn(trimToNull(request.getBodyEn()));
        block.setBodyZht(trimToNull(request.getBodyZht()));
        block.setBodyPt(trimToNull(request.getBodyPt()));
        validateAsset(request.getPrimaryAssetId(), "Primary asset not found");
        block.setPrimaryAssetId(request.getPrimaryAssetId());
        block.setStylePreset(trimToNull(request.getStylePreset()));
        block.setDisplayMode(trimToNull(request.getDisplayMode()));
        block.setVisibilityJson(normalizeJson(request.getVisibilityJson(), "visibilityJson"));
        block.setConfigJson(normalizeJson(request.getConfigJson(), "configJson"));
        block.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "draft");
        block.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        block.setPublishedAt(parseDateTime(request.getPublishedAt()));
        normalizeIds(request.getAttachmentAssetIds()).forEach(id -> validateAsset(id, "Attachment asset not found"));
    }

    private String resolveCode(String candidate, String blockType, Long blockId) {
        if (StringUtils.hasText(candidate)) {
            return candidate.trim();
        }
        String prefix = normalizeBlockType(blockType).replace('_', '-');
        String suffix = blockId == null ? String.valueOf(System.currentTimeMillis()) : String.valueOf(blockId);
        return prefix + "-" + suffix;
    }

    private String normalizeBlockType(String blockType) {
        if (!StringUtils.hasText(blockType)) {
            throw new BusinessException(4002, "blockType is required");
        }
        return blockType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            throw new BusinessException(4002, fieldName + " must be valid JSON");
        }
        return trimmed;
    }

    private void validateAsset(Long assetId, String message) {
        if (assetId == null) {
            return;
        }
        ContentAsset asset = contentAssetMapper.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(4046, message);
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void syncAttachmentLinks(Long blockId, List<Long> attachmentAssetIds, String blockType, String status) {
        contentAssetLinkMapper.delete(new LambdaQueryWrapper<ContentAssetLink>()
                .eq(ContentAssetLink::getEntityType, "story_content_block")
                .eq(ContentAssetLink::getEntityId, blockId));
        List<Long> normalizedIds = normalizeIds(attachmentAssetIds);
        for (int index = 0; index < normalizedIds.size(); index++) {
            Long assetId = normalizedIds.get(index);
            ContentAssetLink link = new ContentAssetLink();
            link.setEntityType("story_content_block");
            link.setEntityId(blockId);
            link.setUsageType("gallery".equalsIgnoreCase(blockType) ? "gallery" : "attachment");
            link.setAssetId(assetId);
            link.setSortOrder(index);
            link.setStatus(StringUtils.hasText(status) ? status : "draft");
            contentAssetLinkMapper.insert(link);
        }
    }

    private StoryContentBlock requireBlock(Long blockId) {
        StoryContentBlock block = storyContentBlockMapper.selectById(blockId);
        if (block == null) {
            throw new BusinessException(4045, "Story content block not found");
        }
        return block;
    }

    private AdminStoryContentBlockResponse toResponse(StoryContentBlock block) {
        List<Long> attachmentAssetIds = contentAssetLinkMapper.selectList(new LambdaQueryWrapper<ContentAssetLink>()
                        .eq(ContentAssetLink::getEntityType, "story_content_block")
                        .eq(ContentAssetLink::getEntityId, block.getId())
                        .orderByAsc(ContentAssetLink::getSortOrder)
                        .orderByAsc(ContentAssetLink::getId))
                .stream()
                .map(ContentAssetLink::getAssetId)
                .filter(Objects::nonNull)
                .toList();
        return AdminStoryContentBlockResponse.builder()
                .id(block.getId())
                .code(block.getCode())
                .blockType(block.getBlockType())
                .titleZh(block.getTitleZh())
                .titleEn(block.getTitleEn())
                .titleZht(block.getTitleZht())
                .titlePt(block.getTitlePt())
                .summaryZh(block.getSummaryZh())
                .summaryEn(block.getSummaryEn())
                .summaryZht(block.getSummaryZht())
                .summaryPt(block.getSummaryPt())
                .bodyZh(block.getBodyZh())
                .bodyEn(block.getBodyEn())
                .bodyZht(block.getBodyZht())
                .bodyPt(block.getBodyPt())
                .primaryAssetId(block.getPrimaryAssetId())
                .attachmentAssetIds(attachmentAssetIds)
                .stylePreset(block.getStylePreset())
                .displayMode(block.getDisplayMode())
                .visibilityJson(block.getVisibilityJson())
                .configJson(block.getConfigJson())
                .status(block.getStatus())
                .sortOrder(block.getSortOrder())
                .publishedAt(block.getPublishedAt())
                .createdAt(block.getCreatedAt())
                .updatedAt(block.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }
}
