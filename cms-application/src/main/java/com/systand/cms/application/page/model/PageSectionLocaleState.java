package com.systand.cms.application.page.model;

import com.systand.cms.api.page.SectionTranslationStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageSectionLocaleState {
    private UUID id;
    private UUID tenantId;
    private UUID siteId;
    private UUID pageId;
    private UUID sectionId;
    private String locale;
    private SectionTranslationStatus translationStatus;
    private Map<String, Object> content;
    private Integer lockVersion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private UUID createdBy;
    private String createdByType;
    private UUID updatedBy;
    private String updatedByType;
}
