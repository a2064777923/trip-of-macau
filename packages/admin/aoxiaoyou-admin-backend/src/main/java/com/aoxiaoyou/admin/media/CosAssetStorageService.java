package com.aoxiaoyou.admin.media;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosAssetStorageService {

    private static final Map<String, String> MIME_TO_EXTENSION = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif",
            "audio/mpeg", "mp3",
            "audio/wav", "wav",
            "application/json", "json",
            "text/plain", "txt"
    );

    private final CosProperties cosProperties;
    private final ObjectProvider<COSClient> cosClientProvider;

    public StoredAssetMetadata storeAsset(MultipartFile file, String assetKind, String localeCode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4055, "Asset file is required");
        }
        if (!cosProperties.isEnabled()) {
            throw new BusinessException(5051, "COS upload is not enabled in the current runtime");
        }

        COSClient cosClient = cosClientProvider.getIfAvailable();
        if (cosClient == null) {
            throw new BusinessException(5052, "COS client is not available");
        }

        try {
            byte[] bytes = file.getBytes();
            String normalizedLocale = StringUtils.hasText(localeCode) ? localeCode.trim() : "";
            String mimeType = resolveMimeType(file);
            String objectKey = buildObjectKey(file.getOriginalFilename(), assetKind, normalizedLocale, mimeType);
            String checksum = sha256(bytes);
            ImageDimensions imageDimensions = extractImageDimensions(bytes);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(bytes.length);
            objectMetadata.setContentType(mimeType);
            if (StringUtils.hasText(file.getOriginalFilename())) {
                objectMetadata.setContentDisposition("inline; filename=\"" + sanitizeHeaderFilename(file.getOriginalFilename()) + "\"");
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosProperties.getBucketName(),
                    objectKey,
                    new ByteArrayInputStream(bytes),
                    objectMetadata
            );
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            return StoredAssetMetadata.builder()
                    .bucketName(cosProperties.getBucketName())
                    .region(cosProperties.getRegion())
                    .objectKey(objectKey)
                    .canonicalUrl(cosProperties.resolvePublicBaseUrl() + "/" + objectKey)
                    .mimeType(mimeType)
                    .localeCode(normalizedLocale)
                    .fileSizeBytes((long) bytes.length)
                    .widthPx(imageDimensions.width())
                    .heightPx(imageDimensions.height())
                    .checksum(checksum)
                    .etag(putObjectResult.getETag())
                    .build();
        } catch (CosClientException ex) {
            throw new BusinessException(5053, "COS upload failed: " + ex.getMessage());
        } catch (IOException ex) {
            throw new BusinessException(5054, "Asset upload payload could not be read");
        }
    }

    public void deleteAsset(String bucketName, String objectKey) {
        if (!StringUtils.hasText(objectKey) || !cosProperties.isEnabled()) {
            return;
        }

        COSClient cosClient = cosClientProvider.getIfAvailable();
        if (cosClient == null) {
            return;
        }

        String resolvedBucketName = StringUtils.hasText(bucketName) ? bucketName : cosProperties.getBucketName();
        try {
            cosClient.deleteObject(resolvedBucketName, objectKey);
        } catch (CosServiceException ex) {
            if (ex.getStatusCode() == 404) {
                return;
            }
            throw new BusinessException(5055, "COS delete failed: " + ex.getMessage());
        } catch (CosClientException ex) {
            throw new BusinessException(5056, "COS delete failed: " + ex.getMessage());
        }
    }

    private String buildObjectKey(String originalFilename, String assetKind, String localeCode, String mimeType) {
        LocalDate now = LocalDate.now();
        String normalizedKind = StringUtils.hasText(assetKind) ? assetKind.trim().toLowerCase(Locale.ROOT) : "other";
        String extension = resolveExtension(originalFilename, mimeType);
        String baseName = StringUtils.hasText(originalFilename)
                ? stripExtension(originalFilename)
                : normalizedKind;
        String slug = slugify(baseName);
        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        StringBuilder pathBuilder = new StringBuilder();
        String basePath = cosProperties.normalizedBasePath();
        if (StringUtils.hasText(basePath)) {
            pathBuilder.append(basePath).append("/");
        }
        pathBuilder.append(normalizedKind)
                .append("/")
                .append(now.getYear())
                .append("/")
                .append(String.format("%02d", now.getMonthValue()))
                .append("/")
                .append(String.format("%02d", now.getDayOfMonth()))
                .append("/");
        if (StringUtils.hasText(localeCode)) {
            pathBuilder.append(localeCode.trim()).append("/");
        }
        pathBuilder.append(slug)
                .append("-")
                .append(uniqueSuffix);
        if (StringUtils.hasText(extension)) {
            pathBuilder.append(".").append(extension);
        }
        return pathBuilder.toString();
    }

    private String resolveMimeType(MultipartFile file) {
        if (StringUtils.hasText(file.getContentType())) {
            return file.getContentType().trim().toLowerCase(Locale.ROOT);
        }
        String guessed = URLConnection.guessContentTypeFromName(file.getOriginalFilename());
        return StringUtils.hasText(guessed) ? guessed.toLowerCase(Locale.ROOT) : "application/octet-stream";
    }

    private String resolveExtension(String originalFilename, String mimeType) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (StringUtils.hasText(extension)) {
            return extension.trim().toLowerCase(Locale.ROOT);
        }
        return MIME_TO_EXTENSION.getOrDefault(mimeType, "");
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    private String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        normalized = normalized.replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "")
                .toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(normalized)) {
            return "asset";
        }
        return normalized.length() > 48 ? normalized.substring(0, 48) : normalized;
    }

    private String sanitizeHeaderFilename(String filename) {
        return filename.replace("\"", "").replace("\r", "").replace("\n", "");
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 digest is not available", ex);
        }
    }

    private ImageDimensions extractImageDimensions(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return new ImageDimensions(null, null);
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        } catch (IOException ex) {
            return new ImageDimensions(null, null);
        }
    }

    private record ImageDimensions(Integer width, Integer height) {
    }
}
