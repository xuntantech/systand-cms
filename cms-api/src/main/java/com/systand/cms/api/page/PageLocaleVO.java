package com.systand.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record PageLocaleVO(
        UUID id,
        UUID siteId,
        UUID pageId,
        String locale,
        String title,
        String summary,
        String seoTitle,
        String seoDescription,
        Map<String, Object> metadata,
        Integer lockVersion,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        UUID createdBy,
        String createdByType,
        UUID updatedBy,
        String updatedByType) {
}
