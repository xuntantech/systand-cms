package com.systand.cms.api.site;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Stable site view; persistence data objects stay internal. */
public record CmsSiteVO(
        UUID id, UUID tenantId, String code, String name, String defaultLocale,
        List<String> enabledLocales, String status, OffsetDateTime createdAt,
        OffsetDateTime updatedAt, UUID createdBy, UUID updatedBy) {
}
