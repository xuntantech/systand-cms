package com.systand.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PageSectionVO(
        UUID id,
        UUID siteId,
        UUID pageId,
        String sectionKey,
        String sectionType,
        String adminLabel,
        String anchorId,
        Integer sortOrder,
        Boolean visible,
        Map<String, Object> settings,
        Integer schemaVersion,
        Integer lockVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID createdBy,
        String createdByType,
        UUID updatedBy,
        String updatedByType) {
}
