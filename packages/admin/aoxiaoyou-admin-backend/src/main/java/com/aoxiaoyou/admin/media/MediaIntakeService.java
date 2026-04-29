package com.aoxiaoyou.admin.media;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetBatchUploadRequest;
import com.aoxiaoyou.admin.dto.request.AdminContentAssetUploadRequest;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.SysAdmin;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.SysAdminMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaIntakeService {

    private static final List<String> ALLOWED_UPLOAD_SOURCES = List.of("picker", "drag-drop", "folder", "clipboard", "replace", "api");

    private final ContentAssetMapper contentAssetMapper;
    private final SysAdminMapper sysAdminMapper;
    private final MediaUploadPolicyService mediaUploadPolicyService;
    private final CosAssetStorageService cosAssetStorageService;
    private final ObjectMapper objectMapper;

    public ContentAsset intakeSingle(AdminContentAssetUploadRequest request, Long adminUserId, String adminUsername) {
        return intake(
                request.getFile(),
                request.getAssetKind(),
                request.getLocaleCode(),
                request.getStatus(),
                request.getUploadSource(),
                request.getClientRelativePath(),
                adminUserId,
                adminUsername
        );
    }

    public List<ContentAsset> intakeBatch(AdminContentAssetBatchUploadRequest request, Long adminUserId, String adminUsername) {
        MultipartFile[] files = request.getFiles();
        if (files == null || files.length == 0) {
            throw new BusinessException(4055, "Asset files are required");
        }
        long totalBytes = 0L;
        for (MultipartFile file : files) {
            totalBytes += file == null ? 0L : file.getSize();
        }
        mediaUploadPolicyService.validateBatch(files.length, totalBytes);

        List<ContentAsset> uploaded = new ArrayList<>();
        String[] relativePaths = request.getClientRelativePaths();
        for (int index = 0; index < files.length; index++) {
            String clientRelativePath = relativePaths != null && index < relativePaths.length ? relativePaths[index] : null;
            uploaded.add(intake(
                    files[index],
                    request.getAssetKind(),
                    request.getLocaleCode(),
                    request.getStatus(),
                    request.getUploadSource(),
                    clientRelativePath,
                    adminUserId,
                    adminUsername
            ));
        }
        return uploaded;
    }

    private ContentAsset intake(
            MultipartFile file,
            String assetKind,
            String localeCode,
            String status,
            String uploadSource,
            String clientRelativePath,
            Long adminUserId,
            String adminUsername) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4055, "Asset file is required");
        }

        SysAdmin admin = requireAdmin(adminUserId);
        String normalizedAssetKind = detectAssetKind(assetKind, file);
        ResolvedMediaUploadPolicy policy = mediaUploadPolicyService.resolvePolicy(normalizedAssetKind, file, admin);
        ProcessedMediaPayload processedPayload = processPayload(file, policy);
        StoredAssetMetadata stored = cosAssetStorageService.storeAsset(StoredAssetPayload.builder()
                .bytes(processedPayload.getBytes())
                .originalFilename(processedPayload.getOriginalFilename())
                .contentType(processedPayload.getContentType())
                .assetKind(policy.getAssetKind())
                .localeCode(localeCode)
                .build());

        ContentAsset asset = new ContentAsset();
        asset.setAssetKind(policy.getAssetKind());
        asset.setBucketName(stored.getBucketName());
        asset.setRegion(stored.getRegion());
        asset.setObjectKey(stored.getObjectKey());
        asset.setCanonicalUrl(stored.getCanonicalUrl());
        asset.setMimeType(stored.getMimeType());
        asset.setAnimationSubtype("lottie".equalsIgnoreCase(policy.getAssetKind()) ? "lottie-json" : null);
        asset.setPosterAssetId(null);
        asset.setFallbackAssetId(null);
        asset.setDefaultLoop("lottie".equalsIgnoreCase(policy.getAssetKind()));
        asset.setDefaultAutoplay("lottie".equalsIgnoreCase(policy.getAssetKind()));
        asset.setLocaleCode(stored.getLocaleCode());
        asset.setOriginalFilename(resolveOriginalFilename(file));
        asset.setFileExtension(resolveFileExtension(processedPayload.getOriginalFilename()));
        asset.setUploadSource(normalizeUploadSource(uploadSource));
        asset.setClientRelativePath(normalizeRelativePath(clientRelativePath));
        asset.setUploadedByAdminId(admin.getId());
        asset.setUploadedByAdminName(StringUtils.hasText(admin.getNickname()) ? admin.getNickname() : adminUsername);
        asset.setFileSizeBytes(stored.getFileSizeBytes());
        asset.setWidthPx(processedPayload.getWidthPx() == null ? stored.getWidthPx() : processedPayload.getWidthPx());
        asset.setHeightPx(processedPayload.getHeightPx() == null ? stored.getHeightPx() : processedPayload.getHeightPx());
        asset.setChecksum(stored.getChecksum());
        asset.setEtag(stored.getEtag());
        asset.setProcessingPolicyCode(policy.getEffectivePolicyCode());
        asset.setProcessingProfileJson(writeJson(buildProcessingProfile(policy, processedPayload)));
        asset.setProcessingStatus(processedPayload.getProcessingStatus());
        asset.setProcessingNote(processedPayload.getProcessingNote());
        asset.setStatus(StringUtils.hasText(status) ? status.trim() : "draft");
        asset.setPublishedAt("published".equalsIgnoreCase(asset.getStatus()) ? LocalDateTime.now() : null);
        contentAssetMapper.insert(asset);
        return asset;
    }

    private ProcessedMediaPayload processPayload(MultipartFile file, ResolvedMediaUploadPolicy policy) {
        try {
            byte[] originalBytes = file.getBytes();
            String originalFilename = resolveOriginalFilename(file);
            String originalContentType = StringUtils.hasText(file.getContentType())
                    ? file.getContentType().trim().toLowerCase(Locale.ROOT)
                    : "application/octet-stream";

            if ("lottie".equalsIgnoreCase(policy.getAssetKind())) {
                LottieMetadata metadata = parseLottieMetadata(originalBytes);
                return ProcessedMediaPayload.builder()
                        .bytes(originalBytes)
                        .originalFilename(rewriteExtension(originalFilename, "json"))
                        .contentType("application/json")
                        .widthPx(metadata.widthPx())
                        .heightPx(metadata.heightPx())
                        .processingStatus("stored")
                        .processingNote(StringUtils.hasText(policy.getNote()) ? policy.getNote() : "Validated as Lottie JSON")
                        .build();
            }

            if (!"image".equals(policy.getPolicyFamily()) || !"image-compressed".equals(policy.getEffectivePolicyCode())) {
                return ProcessedMediaPayload.builder()
                        .bytes(originalBytes)
                        .originalFilename(originalFilename)
                        .contentType(originalContentType)
                        .widthPx(null)
                        .heightPx(null)
                        .processingStatus("stored")
                        .processingNote(policy.getNote())
                        .build();
            }

            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (inputImage == null) {
                return ProcessedMediaPayload.builder()
                        .bytes(originalBytes)
                        .originalFilename(originalFilename)
                        .contentType(originalContentType)
                        .widthPx(null)
                        .heightPx(null)
                        .processingStatus("passthrough")
                        .processingNote("Image processor could not decode the file; original bytes were stored")
                        .build();
            }

            BufferedImage resized = resizeImage(inputImage, policy.getMaxWidthPx(), policy.getMaxHeightPx());
            boolean hasAlpha = resized.getColorModel().hasAlpha();
            String formatName = hasAlpha ? "png" : "jpg";
            String contentType = hasAlpha ? "image/png" : "image/jpeg";
            String processedFilename = rewriteExtension(originalFilename, hasAlpha ? "png" : "jpg");

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName(formatName).hasNext()
                    ? ImageIO.getImageWritersByFormatName(formatName).next()
                    : null;
            if (writer == null) {
                return ProcessedMediaPayload.builder()
                        .bytes(originalBytes)
                        .originalFilename(originalFilename)
                        .contentType(originalContentType)
                        .widthPx(inputImage.getWidth())
                        .heightPx(inputImage.getHeight())
                        .processingStatus("passthrough")
                        .processingNote("No compatible image writer was available; original bytes were stored")
                        .build();
            }

            try (ImageOutputStream outputStream = ImageIO.createImageOutputStream(byteStream)) {
                writer.setOutput(outputStream);
                ImageWriteParam params = writer.getDefaultWriteParam();
                if (!hasAlpha && params.canWriteCompressed()) {
                    params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    int quality = policy.getQualityPercent() == null ? 86 : policy.getQualityPercent();
                    params.setCompressionQuality(Math.max(0.1f, Math.min(1.0f, quality / 100f)));
                }
                writer.write(null, new IIOImage(resized, null, null), params);
            } finally {
                writer.dispose();
            }

            byte[] processedBytes = byteStream.toByteArray();
            if (processedBytes.length == 0) {
                processedBytes = originalBytes;
                contentType = originalContentType;
                processedFilename = originalFilename;
            }

            return ProcessedMediaPayload.builder()
                    .bytes(processedBytes)
                    .originalFilename(processedFilename)
                    .contentType(contentType)
                    .widthPx(resized.getWidth())
                    .heightPx(resized.getHeight())
                    .processingStatus("processed")
                    .processingNote(StringUtils.hasText(policy.getNote()) ? policy.getNote() : "Image was processed according to the current media policy")
                    .build();
        } catch (Exception ex) {
            throw new BusinessException(5057, "Media processing failed: " + ex.getMessage());
        }
    }

    private String detectAssetKind(String assetKind, MultipartFile file) {
        String normalized = mediaUploadPolicyService.normalizeAssetKind(assetKind, file);
        if ("lottie".equals(normalized)) {
            return "lottie";
        }
        boolean candidateJson = "json".equals(normalized)
                || (!StringUtils.hasText(assetKind) && hasJsonFilename(file));
        if (!candidateJson) {
            return normalized;
        }
        try {
            byte[] bytes = file == null ? new byte[0] : file.getBytes();
            parseLottieMetadata(bytes);
            return "lottie";
        } catch (Exception ignored) {
            return normalized;
        }
    }

    private boolean hasJsonFilename(MultipartFile file) {
        String filename = file == null ? null : file.getOriginalFilename();
        return StringUtils.hasText(filename) && filename.trim().toLowerCase(Locale.ROOT).endsWith(".json");
    }

    private LottieMetadata parseLottieMetadata(byte[] bytes) {
        try {
            Map<?, ?> payload = objectMapper.readValue(bytes, Map.class);
            Object version = payload.get("v");
            Object frameRate = payload.get("fr");
            Object layers = payload.get("layers");
            Object width = payload.get("w");
            Object height = payload.get("h");
            if (version == null || frameRate == null || !(layers instanceof List<?>)) {
                throw new BusinessException(4002, "JSON payload is not a valid Lottie document");
            }
            Integer widthPx = toInteger(width);
            Integer heightPx = toInteger(height);
            return new LottieMetadata(widthPx, heightPx);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception ex) {
            throw new BusinessException(4002, "JSON payload is not a valid Lottie document");
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    private BufferedImage resizeImage(BufferedImage inputImage, Integer maxWidthPx, Integer maxHeightPx) {
        int sourceWidth = inputImage.getWidth();
        int sourceHeight = inputImage.getHeight();
        int targetWidth = sourceWidth;
        int targetHeight = sourceHeight;

        if (maxWidthPx != null && maxWidthPx > 0 && targetWidth > maxWidthPx) {
            double ratio = maxWidthPx / (double) targetWidth;
            targetWidth = maxWidthPx;
            targetHeight = Math.max(1, (int) Math.round(targetHeight * ratio));
        }
        if (maxHeightPx != null && maxHeightPx > 0 && targetHeight > maxHeightPx) {
            double ratio = maxHeightPx / (double) targetHeight;
            targetHeight = maxHeightPx;
            targetWidth = Math.max(1, (int) Math.round(targetWidth * ratio));
        }
        if (targetWidth == sourceWidth && targetHeight == sourceHeight) {
            return inputImage;
        }

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, inputImage.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }

    private Map<String, Object> buildProcessingProfile(ResolvedMediaUploadPolicy policy, ProcessedMediaPayload payload) {
        Map<String, Object> profile = new LinkedHashMap<>(policy.getSnapshot());
        profile.put("processingStatus", payload.getProcessingStatus());
        profile.put("processingNote", payload.getProcessingNote());
        profile.put("outputContentType", payload.getContentType());
        profile.put("outputFilename", payload.getOriginalFilename());
        profile.put("outputWidthPx", payload.getWidthPx());
        profile.put("outputHeightPx", payload.getHeightPx());
        profile.put("outputSizeBytes", payload.getBytes() == null ? 0 : payload.getBytes().length);
        return profile;
    }

    private SysAdmin requireAdmin(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(4010, "Admin identity is required for media uploads");
        }
        SysAdmin admin = sysAdminMapper.selectById(adminUserId);
        if (admin == null) {
            throw new BusinessException(4041, "Admin account not found");
        }
        return admin;
    }

    private String resolveOriginalFilename(MultipartFile file) {
        if (file == null || !StringUtils.hasText(file.getOriginalFilename())) {
            return "asset.bin";
        }
        return file.getOriginalFilename().trim();
    }

    private String resolveFileExtension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        return extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
    }

    private String rewriteExtension(String filename, String extension) {
        String baseName = filename;
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = filename.substring(0, dotIndex);
        }
        return baseName + "." + extension;
    }

    private String normalizeUploadSource(String uploadSource) {
        if (!StringUtils.hasText(uploadSource)) {
            return "picker";
        }
        String normalized = uploadSource.trim().toLowerCase(Locale.ROOT);
        return ALLOWED_UPLOAD_SOURCES.contains(normalized) ? normalized : "api";
    }

    private String normalizeRelativePath(String clientRelativePath) {
        if (!StringUtils.hasText(clientRelativePath)) {
            return null;
        }
        return clientRelativePath.replace("\\", "/").trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5003, "Failed to serialize media processing profile");
        }
    }

    private record LottieMetadata(Integer widthPx, Integer heightPx) {}
}
