package com.systand.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PageVO(
        UUID id,
        UUID siteId,
        String code,
        String routePath,
        PageKind pageKind,
        String templateCode,
        PagePublicationStatus publicationStatus,
        Boolean home,
        Integer sortOrder,
        Map<String, Object> layoutSettings,
        OffsetDateTime scheduledPublishAt,
        OffsetDateTime scheduledUnpublishAt,
        OffsetDateTime publishedAt,
        OffsetDateTime firstPublishedAt,
        OffsetDateTime unpublishedAt,
        OffsetDateTime archivedAt,
        Integer lockVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID createdBy,
        String createdByType,
        UUID updatedBy,
        String updatedByType) {
}
