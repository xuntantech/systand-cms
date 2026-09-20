package com.systand.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PageSectionLocaleVO(
        UUID id,
        UUID siteId,
        UUID pageId,
        UUID sectionId,
        String locale,
        SectionTranslationStatus translationStatus,
        Map<String, Object> content,
        Integer lockVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID createdBy,
        String createdByType,
        UUID updatedBy,
        String updatedByType) {
}
