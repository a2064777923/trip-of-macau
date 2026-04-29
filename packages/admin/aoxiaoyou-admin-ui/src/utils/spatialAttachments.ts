import type { AdminSpatialAssetLinkItem } from '../types/admin';

export type SpatialAttachmentDraft = AdminSpatialAssetLinkItem & {
  assetIds?: number[] | null;
};

function normalizeIds(values?: Array<number | null | undefined> | null) {
  const seen = new Set<number>();
  const normalized: number[] = [];

  (values || []).forEach((value) => {
    if (typeof value !== 'number' || Number.isNaN(value) || seen.has(value)) {
      return;
    }
    seen.add(value);
    normalized.push(value);
  });

  return normalized;
}

export function hydrateSpatialAttachmentDrafts(
  attachments?: AdminSpatialAssetLinkItem[] | null,
): SpatialAttachmentDraft[] {
  return (attachments || []).map((attachment) => ({
    ...attachment,
    assetIds:
      attachment.usageType === 'gallery' && typeof attachment.assetId === 'number'
        ? [attachment.assetId]
        : undefined,
  }));
}

export function hasSpatialAttachmentSelection(
  attachment?: SpatialAttachmentDraft | null,
) {
  if (!attachment) {
    return false;
  }

  if (attachment.usageType === 'gallery') {
    return normalizeIds(
      attachment.assetIds?.length ? attachment.assetIds : [attachment.assetId],
    ).length > 0;
  }

  return typeof attachment.assetId === 'number' || normalizeIds(attachment.assetIds).length > 0;
}

export function normalizeSpatialAttachmentDrafts(
  attachments?: Array<SpatialAttachmentDraft | null | undefined>,
): AdminSpatialAssetLinkItem[] {
  const normalized: AdminSpatialAssetLinkItem[] = [];

  (attachments || []).forEach((attachment) => {
    if (!attachment) {
      return;
    }

    const candidateIds =
      attachment.usageType === 'gallery'
        ? normalizeIds(
            attachment.assetIds?.length ? attachment.assetIds : [attachment.assetId],
          )
        : normalizeIds(
            typeof attachment.assetId === 'number'
              ? [attachment.assetId]
              : attachment.assetIds,
          ).slice(0, 1);

    candidateIds.forEach((assetId, index) => {
      const { assetIds, ...rest } = attachment;
      normalized.push({
        ...rest,
        assetId,
        sortOrder:
          typeof attachment.sortOrder === 'number'
            ? attachment.sortOrder + index
            : normalized.length,
      });
    });
  });

  return normalized;
}

export function collectSpatialAttachmentAssetIds(
  attachments?: AdminSpatialAssetLinkItem[] | null,
) {
  return normalizeIds((attachments || []).map((attachment) => attachment.assetId));
}
