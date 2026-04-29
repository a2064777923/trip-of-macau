package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminIndoorFloorResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerResponse;
import com.aoxiaoyou.admin.dto.response.AdminSpatialAssetLinkResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorTilePreviewResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.CosProperties;
import com.aoxiaoyou.admin.media.StoredAssetMetadata;
import com.aoxiaoyou.admin.media.StoredAssetPayload;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class IndoorTilePipelineService {

    private static final String FLOOR_ATTACHMENT_OWNER = "indoor_floor";
    private static final String ATTACHMENT_RELATION = "attachment_asset";
    private static final String CONTENT_ASSET_TARGET = "content_asset";
    private static final String CONFIG_INDOOR_MIN_SCALE_METERS = "indoor.zoom.min-scale-meters";
    private static final String CONFIG_INDOOR_MAX_SCALE_METERS = "indoor.zoom.max-scale-meters";
    private static final String CONFIG_INDOOR_VIEWPORT_PX = "indoor.zoom.reference-viewport-px";
    private static final String CONFIG_TILE_SIZE_PX = "indoor.tile.default-size-px";
    private static final Set<String> ALLOWED_TILE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final DateTimeFormatter TILE_RUN_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final IndoorFloorMapper indoorFloorMapper;
    private final BuildingMapper buildingMapper;
    private final SysConfigMapper sysConfigMapper;
    private final CosAssetStorageService cosAssetStorageService;
    private final CosProperties cosProperties;
    private final AdminContentRelationService adminContentRelationService;
    private final AdminSpatialAssetLinkService adminSpatialAssetLinkService;
    private final IndoorMarkerAuthoringService indoorMarkerAuthoringService;
    private final ObjectMapper objectMapper;

    public AdminIndoorFloorResponse getFloorDetail(Long floorId) {
        IndoorFloor floor = requireFloor(floorId);
        Building building = requireBuilding(floor.getBuildingId());
        return toFloorResponse(floor, building, indoorMarkerAuthoringService.listFloorMarkers(floorId, null));
    }

    public AdminIndoorTilePreviewResponse previewFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx) {
        requireFloor(floorId);
        ZipTilePreview preview = analyzeZipTiles(file, normalizeTileSize(tileSizePx), false);
        ZoomDerivation derivation = deriveZoom(preview.imageWidthPx(), preview.imageHeightPx(), requireFloor(floorId).getAreaSqm());
        return AdminIndoorTilePreviewResponse.builder()
                .floorId(floorId)
                .sourceType("zip")
                .sourceFilename(fileName(file))
                .imageWidthPx(preview.imageWidthPx())
                .imageHeightPx(preview.imageHeightPx())
                .tileSizePx(preview.tileSizePx())
                .gridCols(preview.gridCols())
                .gridRows(preview.gridRows())
                .tileLevelCount(preview.levelCount())
                .tileEntryCount(preview.entryCount())
                .zoomMin(derivation.zoomMin())
                .defaultZoom(derivation.defaultZoom())
                .zoomMax(derivation.zoomMax())
                .derivationJson(derivation.derivationJson())
                .manifestJson(preview.manifestJson())
                .notes(preview.notes())
                .build();
    }

    public AdminIndoorFloorResponse importFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId) {
        IndoorFloor floor = requireFloor(floorId);
        Building building = requireBuilding(floor.getBuildingId());
        ZipTilePreview preview = analyzeZipTiles(file, normalizeTileSize(tileSizePx), true);
        String importRunId = TILE_RUN_FORMATTER.format(LocalDateTime.now());
        String rootPrefix = buildIndoorRootPrefix(floorId, importRunId, "tiles");
        List<Map<String, Object>> tileEntries = new ArrayList<>();
        String tileRootUrl = null;

        for (ZipTileEntry entry : preview.entries()) {
          String objectKey = String.format("%s/%d/%d_%d.%s", rootPrefix, entry.z(), entry.x(), entry.y(), entry.extension());
          StoredAssetMetadata stored = cosAssetStorageService.storeAsset(
                  StoredAssetPayload.builder()
                          .bytes(entry.bytes())
                          .originalFilename(entry.filename())
                          .contentType(contentTypeForExtension(entry.extension()))
                          .assetKind("indoor-tile")
                          .build(),
                  objectKey
          );
          if (tileRootUrl == null) {
              tileRootUrl = resolveRootUrl(stored.getCanonicalUrl(), stored.getObjectKey(), rootPrefix);
          }
          tileEntries.add(Map.of(
                  "z", entry.z(),
                  "x", entry.x(),
                  "y", entry.y(),
                  "url", stored.getCanonicalUrl()
          ));
        }

        StoredAssetMetadata previewStored = cosAssetStorageService.storeAsset(
                StoredAssetPayload.builder()
                        .bytes(writePng(buildZipPreview(preview)))
                        .originalFilename(String.format("floor-%d-zip-preview.png", floorId))
                        .contentType("image/png")
                        .assetKind("indoor-floor-preview")
                        .build(),
                String.format("%s/preview.png", rootPrefix)
        );

        ZoomDerivation derivation = deriveZoom(preview.imageWidthPx(), preview.imageHeightPx(), floor.getAreaSqm());
        floor.setTileSourceType("zip");
        floor.setTileSourceAssetId(null);
        floor.setTileSourceFilename(fileName(file));
        floor.setTilePreviewImageUrl(previewStored.getCanonicalUrl());
        if (!StringUtils.hasText(floor.getFloorPlanUrl())) {
            floor.setFloorPlanUrl(previewStored.getCanonicalUrl());
        }
        floor.setTileRootUrl(tileRootUrl);
        floor.setTileManifestJson(writeJson(Map.of(
                "sourceType", "zip",
                "defaultLevel", preview.defaultLevel(),
                "levels", preview.levelSummaries(),
                "gridCols", preview.gridCols(),
                "gridRows", preview.gridRows(),
                "tileSizePx", preview.tileSizePx(),
                "tiles", tileEntries
        )));
        floor.setTileZoomDerivationJson(derivation.derivationJson());
        floor.setImageWidthPx(preview.imageWidthPx());
        floor.setImageHeightPx(preview.imageHeightPx());
        floor.setTileSizePx(preview.tileSizePx());
        floor.setGridCols(preview.gridCols());
        floor.setGridRows(preview.gridRows());
        floor.setTileLevelCount(preview.levelCount());
        floor.setTileEntryCount(preview.entryCount());
        floor.setZoomMin(derivation.zoomMin());
        floor.setDefaultZoom(derivation.defaultZoom());
        floor.setZoomMax(derivation.zoomMax());
        floor.setImportStatus("ready");
        floor.setImportNote(String.format("Zip import completed with %d tiles.", preview.entryCount()));
        floor.setImportedAt(LocalDateTime.now());
        indoorFloorMapper.updateById(floor);
        return toFloorResponse(requireFloor(floorId), building, indoorMarkerAuthoringService.listFloorMarkers(floorId, null));
    }

    public AdminIndoorFloorResponse importFloorPlanImage(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId) {
        IndoorFloor floor = requireFloor(floorId);
        Building building = requireBuilding(floor.getBuildingId());
        BufferedImage image = readImage(file);
        int normalizedTileSize = normalizeTileSize(tileSizePx);
        int gridCols = (int) Math.ceil(image.getWidth() / (double) normalizedTileSize);
        int gridRows = (int) Math.ceil(image.getHeight() / (double) normalizedTileSize);
        String importRunId = TILE_RUN_FORMATTER.format(LocalDateTime.now());
        String rootPrefix = buildIndoorRootPrefix(floorId, importRunId, "image-import");
        List<Map<String, Object>> tileEntries = new ArrayList<>();
        String tileRootUrl = null;

        for (int row = 0; row < gridRows; row++) {
            for (int col = 0; col < gridCols; col++) {
                BufferedImage tile = image.getSubimage(
                        col * normalizedTileSize,
                        row * normalizedTileSize,
                        Math.min(normalizedTileSize, image.getWidth() - col * normalizedTileSize),
                        Math.min(normalizedTileSize, image.getHeight() - row * normalizedTileSize)
                );
                String objectKey = String.format("%s/0/%d_%d.png", rootPrefix, col, row);
                StoredAssetMetadata stored = cosAssetStorageService.storeAsset(
                        StoredAssetPayload.builder()
                                .bytes(writePng(tile))
                                .originalFilename(String.format("floor-%d-%d-%d.png", floorId, col, row))
                                .contentType("image/png")
                                .assetKind("indoor-tile")
                                .build(),
                        objectKey
                );
                if (tileRootUrl == null) {
                    tileRootUrl = resolveRootUrl(stored.getCanonicalUrl(), stored.getObjectKey(), rootPrefix);
                }
                tileEntries.add(Map.of(
                        "z", 0,
                        "x", col,
                        "y", row,
                        "url", stored.getCanonicalUrl()
                ));
            }
        }

        StoredAssetMetadata previewStored = cosAssetStorageService.storeAsset(
                StoredAssetPayload.builder()
                        .bytes(writePng(image))
                        .originalFilename(fileName(file))
                        .contentType("image/png")
                        .assetKind("indoor-floor-preview")
                        .build(),
                String.format("%s/preview.png", rootPrefix)
        );

        ZoomDerivation derivation = deriveZoom(image.getWidth(), image.getHeight(), floor.getAreaSqm());
        floor.setTileSourceType("image-sliced");
        floor.setTileSourceAssetId(null);
        floor.setTileSourceFilename(fileName(file));
        floor.setTilePreviewImageUrl(previewStored.getCanonicalUrl());
        floor.setFloorPlanUrl(previewStored.getCanonicalUrl());
        floor.setTileRootUrl(tileRootUrl);
        floor.setTileManifestJson(writeJson(Map.of(
                "sourceType", "image-sliced",
                "defaultLevel", 0,
                "levels", List.of(Map.of("z", 0, "gridCols", gridCols, "gridRows", gridRows, "tileCount", tileEntries.size())),
                "gridCols", gridCols,
                "gridRows", gridRows,
                "tileSizePx", normalizedTileSize,
                "tiles", tileEntries
        )));
        floor.setTileZoomDerivationJson(derivation.derivationJson());
        floor.setImageWidthPx(image.getWidth());
        floor.setImageHeightPx(image.getHeight());
        floor.setTileSizePx(normalizedTileSize);
        floor.setGridCols(gridCols);
        floor.setGridRows(gridRows);
        floor.setTileLevelCount(1);
        floor.setTileEntryCount(tileEntries.size());
        floor.setZoomMin(derivation.zoomMin());
        floor.setDefaultZoom(derivation.defaultZoom());
        floor.setZoomMax(derivation.zoomMax());
        floor.setImportStatus("ready");
        floor.setImportNote(String.format("Image slicing completed with %d tiles.", tileEntries.size()));
        floor.setImportedAt(LocalDateTime.now());
        indoorFloorMapper.updateById(floor);
        return toFloorResponse(requireFloor(floorId), building, indoorMarkerAuthoringService.listFloorMarkers(floorId, null));
    }

    private IndoorFloor requireFloor(Long floorId) {
        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
        if (floor == null) {
            throw new BusinessException(4046, "floor not found");
        }
        return floor;
    }

    private Building requireBuilding(Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException(4040, "building not found");
        }
        return building;
    }

    private AdminIndoorFloorResponse toFloorResponse(IndoorFloor floor, Building building, List<AdminIndoorMarkerResponse> markers) {
        List<AdminSpatialAssetLinkResponse> attachmentLinks = adminSpatialAssetLinkService.listLinks("indoor_floor", floor.getId());
        List<Long> legacyAttachmentIds = adminContentRelationService.listTargetIds(FLOOR_ATTACHMENT_OWNER, floor.getId(), ATTACHMENT_RELATION, CONTENT_ASSET_TARGET);
        List<AdminSpatialAssetLinkResponse> resolvedAttachmentLinks = attachmentLinks.isEmpty()
                ? legacyAttachmentIds.stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(assetId -> AdminSpatialAssetLinkResponse.builder()
                                .assetId(assetId)
                                .usageType("gallery")
                                .status("draft")
                                .build())
                        .toList()
                : attachmentLinks;
        return AdminIndoorFloorResponse.builder()
                .id(floor.getId())
                .buildingId(building.getId())
                .indoorMapId(floor.getIndoorMapId())
                .floorCode(floor.getFloorCode())
                .floorNumber(floor.getFloorNumber())
                .floorNameZh(floor.getFloorNameZh())
                .floorNameEn(floor.getFloorNameEn())
                .floorNameZht(floor.getFloorNameZht())
                .floorNamePt(floor.getFloorNamePt())
                .descriptionZh(floor.getDescriptionZh())
                .descriptionEn(floor.getDescriptionEn())
                .descriptionZht(floor.getDescriptionZht())
                .descriptionPt(floor.getDescriptionPt())
                .coverAssetId(floor.getCoverAssetId())
                .floorPlanAssetId(floor.getFloorPlanAssetId())
                .floorPlanUrl(floor.getFloorPlanUrl())
                .tileSourceType(floor.getTileSourceType())
                .tileSourceAssetId(floor.getTileSourceAssetId())
                .tileSourceFilename(floor.getTileSourceFilename())
                .tilePreviewImageUrl(floor.getTilePreviewImageUrl())
                .tileRootUrl(floor.getTileRootUrl())
                .tileManifestJson(floor.getTileManifestJson())
                .tileZoomDerivationJson(floor.getTileZoomDerivationJson())
                .imageWidthPx(floor.getImageWidthPx())
                .imageHeightPx(floor.getImageHeightPx())
                .tileSizePx(floor.getTileSizePx())
                .gridCols(floor.getGridCols())
                .gridRows(floor.getGridRows())
                .tileLevelCount(floor.getTileLevelCount())
                .tileEntryCount(floor.getTileEntryCount())
                .importStatus(floor.getImportStatus())
                .importNote(floor.getImportNote())
                .importedAt(floor.getImportedAt())
                .altitudeMeters(floor.getAltitudeMeters())
                .areaSqm(floor.getAreaSqm())
                .zoomMin(floor.getZoomMin())
                .zoomMax(floor.getZoomMax())
                .defaultZoom(floor.getDefaultZoom())
                .popupConfigJson(floor.getPopupConfigJson())
                .displayConfigJson(floor.getDisplayConfigJson())
                .attachments(resolvedAttachmentLinks)
                .attachmentAssetIds((resolvedAttachmentLinks.isEmpty() ? legacyAttachmentIds : resolvedAttachmentLinks.stream()
                        .map(AdminSpatialAssetLinkResponse::getAssetId)
                        .filter(Objects::nonNull)
                        .toList()))
                .markerCount(markers.size())
                .sortOrder(floor.getSortOrder())
                .status(floor.getStatus())
                .publishedAt(floor.getPublishedAt())
                .createdAt(floor.getCreatedAt())
                .updatedAt(floor.getUpdatedAt())
                .markers(markers)
                .build();
    }

    private int normalizeTileSize(Integer requested) {
        int fallback = readIntConfig(CONFIG_TILE_SIZE_PX, 512);
        int value = requested == null ? fallback : requested;
        if (value < 128) {
            return 128;
        }
        if (value > 1024) {
            return 1024;
        }
        return value;
    }

    private ZipTilePreview analyzeZipTiles(MultipartFile file, int tileSizePx, boolean includeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4055, "Tile zip file is required");
        }
        String filename = fileName(file);
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new BusinessException(4001, "Tile import only accepts zip archives");
        }

        List<ZipTileEntry> entries = new ArrayList<>();
        Set<String> seenCoordinates = new LinkedHashSet<>();
        List<String> notes = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String normalizedPath = zipEntry.getName().replace("\\", "/");
                if (normalizedPath.contains("../")) {
                    throw new BusinessException(4001, "Zip archive contains an invalid entry path");
                }
                TileCoordinate coordinate = parseTileCoordinate(normalizedPath);
                if (coordinate == null) {
                    continue;
                }
                String dedupeKey = coordinate.z() + ":" + coordinate.x() + ":" + coordinate.y();
                if (!seenCoordinates.add(dedupeKey)) {
                    throw new BusinessException(4001, "Zip archive contains duplicate tile coordinates");
                }
                byte[] bytes = zipInputStream.readAllBytes();
                BufferedImage image = readImage(bytes, normalizedPath);
                entries.add(new ZipTileEntry(
                        coordinate.z(),
                        coordinate.x(),
                        coordinate.y(),
                        coordinate.extension(),
                        normalizedPath,
                        includeBytes ? bytes : null,
                        image.getWidth(),
                        image.getHeight()
                ));
            }
        } catch (IOException ex) {
            throw new BusinessException(5057, "Failed to read tile zip: " + ex.getMessage());
        }

        if (entries.isEmpty()) {
            throw new BusinessException(4001, "No valid tile images were found in the archive");
        }

        int defaultLevel = entries.stream().mapToInt(ZipTileEntry::z).max().orElse(0);
        List<ZipTileEntry> defaultEntries = entries.stream().filter(item -> item.z() == defaultLevel).toList();
        int gridCols = defaultEntries.stream().mapToInt(ZipTileEntry::x).max().orElse(0) + 1;
        int gridRows = defaultEntries.stream().mapToInt(ZipTileEntry::y).max().orElse(0) + 1;
        int imageWidthPx = defaultEntries.stream().mapToInt(ZipTileEntry::widthPx).max().orElse(tileSizePx) * gridCols;
        int imageHeightPx = defaultEntries.stream().mapToInt(ZipTileEntry::heightPx).max().orElse(tileSizePx) * gridRows;
        List<Map<String, Object>> levels = entries.stream()
                .collect(Collectors.groupingBy(ZipTileEntry::z, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> level = new LinkedHashMap<>();
                    level.put("z", entry.getKey());
                    level.put("gridCols", entry.getValue().stream().mapToInt(ZipTileEntry::x).max().orElse(0) + 1);
                    level.put("gridRows", entry.getValue().stream().mapToInt(ZipTileEntry::y).max().orElse(0) + 1);
                    level.put("tileCount", entry.getValue().size());
                    return level;
                })
                .toList();
        notes.add(String.format("Detected %d tiles across %d zoom level(s).", entries.size(), levels.size()));

        String manifestJson = writeJson(Map.of(
                "sourceType", "zip",
                "defaultLevel", defaultLevel,
                "gridCols", gridCols,
                "gridRows", gridRows,
                "tileSizePx", tileSizePx,
                "levels", levels,
                "tiles", entries.stream().map(entry -> Map.of(
                        "z", entry.z(),
                        "x", entry.x(),
                        "y", entry.y(),
                        "path", entry.filename()
                )).toList()
        ));

        return new ZipTilePreview(entries, defaultLevel, imageWidthPx, imageHeightPx, tileSizePx, gridCols, gridRows, levels.size(), entries.size(), levels, manifestJson, notes);
    }

    private TileCoordinate parseTileCoordinate(String normalizedPath) {
        String lower = normalizedPath.toLowerCase(Locale.ROOT);
        int dotIndex = lower.lastIndexOf('.');
        if (dotIndex < 0) {
            return null;
        }
        String extension = lower.substring(dotIndex + 1);
        if (!ALLOWED_TILE_EXTENSIONS.contains(extension)) {
            return null;
        }

        String[] segments = normalizedPath.substring(0, dotIndex).split("/");
        try {
            if (segments.length >= 3 && isInteger(segments[segments.length - 3]) && isInteger(segments[segments.length - 2]) && isInteger(segments[segments.length - 1])) {
                return new TileCoordinate(
                        Integer.parseInt(segments[segments.length - 3]),
                        Integer.parseInt(segments[segments.length - 2]),
                        Integer.parseInt(segments[segments.length - 1]),
                        extension
                );
            }
            String[] parts = segments[segments.length - 1].split("[_-]");
            if (parts.length == 2 && isInteger(parts[0]) && isInteger(parts[1])) {
                int z = segments.length >= 2 && isInteger(segments[segments.length - 2]) ? Integer.parseInt(segments[segments.length - 2]) : 0;
                return new TileCoordinate(z, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), extension);
            }
        } catch (NumberFormatException ignore) {
            return null;
        }
        return null;
    }

    private BufferedImage readImage(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException(4001, "Indoor floor image could not be decoded");
            }
            if (image.getWidth() > 12000 || image.getHeight() > 12000) {
                throw new BusinessException(4001, "Indoor floor image is too large to process");
            }
            return image;
        } catch (IOException ex) {
            throw new BusinessException(5057, "Failed to read indoor floor image: " + ex.getMessage());
        }
    }

    private BufferedImage readImage(byte[] bytes, String filename) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BusinessException(4001, "Tile entry could not be decoded: " + filename);
            }
            return image;
        } catch (IOException ex) {
            throw new BusinessException(4001, "Tile entry could not be decoded: " + filename);
        }
    }

    private BufferedImage buildZipPreview(ZipTilePreview preview) {
        BufferedImage canvas = new BufferedImage(preview.imageWidthPx(), preview.imageHeightPx(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            for (ZipTileEntry entry : preview.entries()) {
                if (entry.z() != preview.defaultLevel() || entry.bytes() == null) {
                    continue;
                }
                BufferedImage tileImage = readImage(entry.bytes(), entry.filename());
                graphics.drawImage(tileImage, entry.x() * preview.tileSizePx(), entry.y() * preview.tileSizePx(), null);
            }
        } finally {
            graphics.dispose();
        }
        return canvas;
    }

    private byte[] writePng(BufferedImage image) {
        try {
            BufferedImage normalized = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = normalized.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(image, 0, 0, null);
            } finally {
                graphics.dispose();
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(normalized, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BusinessException(5057, "Failed to encode indoor floor image");
        }
    }

    private ZoomDerivation deriveZoom(Integer imageWidthPx, Integer imageHeightPx, BigDecimal areaSqm) {
        double minScaleMeters = readDoubleConfig(CONFIG_INDOOR_MIN_SCALE_METERS, 20d);
        double maxScaleMeters = readDoubleConfig(CONFIG_INDOOR_MAX_SCALE_METERS, 0.5d);
        double viewportPx = readDoubleConfig(CONFIG_INDOOR_VIEWPORT_PX, 390d);
        if (imageWidthPx == null || imageHeightPx == null || imageWidthPx <= 0 || imageHeightPx <= 0) {
            return new ZoomDerivation(new BigDecimal("0.50"), new BigDecimal("1.00"), new BigDecimal("2.50"), writeJson(Map.of("mode", "fallback")));
        }

        double safeArea = areaSqm == null || areaSqm.compareTo(BigDecimal.ONE) < 0 ? 200d : areaSqm.doubleValue();
        double aspectRatio = imageWidthPx / (double) imageHeightPx;
        double estimatedWidthMeters = Math.sqrt(safeArea * aspectRatio);
        double estimatedHeightMeters = safeArea / estimatedWidthMeters;
        double metersPerPixel = estimatedWidthMeters / imageWidthPx;
        double minZoom = clamp(metersPerPixel * viewportPx / minScaleMeters, 0.2d, 16d);
        double maxZoom = clamp(metersPerPixel * viewportPx / maxScaleMeters, 0.5d, 24d);
        double defaultZoom = clamp(Math.sqrt(minZoom * maxZoom), minZoom, maxZoom);
        return new ZoomDerivation(
                scaleDecimal(minZoom),
                scaleDecimal(defaultZoom),
                scaleDecimal(maxZoom),
                writeJson(Map.of(
                        "mode", "derived",
                        "areaSqm", safeArea,
                        "imageWidthPx", imageWidthPx,
                        "imageHeightPx", imageHeightPx,
                        "estimatedWidthMeters", scaleDecimal(estimatedWidthMeters),
                        "estimatedHeightMeters", scaleDecimal(estimatedHeightMeters),
                        "metersPerPixel", scaleDecimal(metersPerPixel),
                        "minScaleMeters", scaleDecimal(minScaleMeters),
                        "maxScaleMeters", scaleDecimal(maxScaleMeters),
                        "referenceViewportPx", scaleDecimal(viewportPx)
                ))
        );
    }

    private int readIntConfig(String key, int fallback) {
        SysConfig config = sysConfigMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return fallback;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    private double readDoubleConfig(String key, double fallback) {
        SysConfig config = sysConfigMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return fallback;
        }
        try {
            return Double.parseDouble(config.getConfigValue().trim());
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    private String fileName(MultipartFile file) {
        return StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : "upload.bin";
    }

    private String contentTypeForExtension(String extension) {
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            default -> "image/png";
        };
    }

    private String resolveRootUrl(String canonicalUrl, String objectKey, String rootPrefix) {
        int objectKeyIndex = canonicalUrl.indexOf(objectKey);
        if (objectKeyIndex < 0) {
            return canonicalUrl;
        }
        return canonicalUrl.substring(0, objectKeyIndex) + rootPrefix;
    }

    private String buildIndoorRootPrefix(Long floorId, String importRunId, String suffix) {
        String relativePrefix = String.format("indoor/floors/%d/%s/%s", floorId, importRunId, suffix);
        if (!StringUtils.hasText(cosProperties.normalizedBasePath())) {
            return relativePrefix;
        }
        return cosProperties.normalizedBasePath() + "/" + relativePrefix;
    }

    private boolean isInteger(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5003, "Failed to serialize indoor tile payload");
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private BigDecimal scaleDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private record ZipTileEntry(int z, int x, int y, String extension, String filename, byte[] bytes, int widthPx, int heightPx) {
    }

    private record ZipTilePreview(
            List<ZipTileEntry> entries,
            int defaultLevel,
            int imageWidthPx,
            int imageHeightPx,
            int tileSizePx,
            int gridCols,
            int gridRows,
            int levelCount,
            int entryCount,
            List<Map<String, Object>> levelSummaries,
            String manifestJson,
            List<String> notes
    ) {
    }

    private record TileCoordinate(int z, int x, int y, String extension) {
    }

    private record ZoomDerivation(BigDecimal zoomMin, BigDecimal defaultZoom, BigDecimal zoomMax, String derivationJson) {
    }
}
