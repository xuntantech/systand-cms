package com.systand.cms.application.page.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageLocaleState {
    private UUID id;
    private UUID tenantId;
    private UUID siteId;
    private UUID pageId;
    private String locale;
    private String title;
    private String summary;
    private String seoTitle;
    private String seoDescription;
    private Map<String, Object> metadata;
    private Integer lockVersion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private UUID createdBy;
    private String createdByType;
    private UUID updatedBy;
    private String updatedByType;
}
