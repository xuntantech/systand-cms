package com.systand.cms.application.page.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageSectionState {
    private UUID id;
    private UUID tenantId;
    private UUID siteId;
    private UUID pageId;
    private String sectionKey;
    private String sectionType;
    private String adminLabel;
    private String anchorId;
    private Integer sortOrder;
    private Boolean visible;
    private Map<String, Object> settings;
    private Integer schemaVersion;
    private Integer lockVersion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private UUID createdBy;
    private String createdByType;
    private UUID updatedBy;
    private String updatedByType;
}
