package com.systand.cms.application.page.model;

import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageState {
    private UUID id;
    private UUID tenantId;
    private UUID siteId;
    private String code;
    private String routePath;
    private PageKind pageKind;
    private String templateCode;
    private PagePublicationStatus publicationStatus;
    private Boolean home;
    private Map<String, Object> layoutSettings;
    private Integer sortOrder;
    private OffsetDateTime scheduledPublishAt;
    private OffsetDateTime scheduledUnpublishAt;
    private OffsetDateTime publishedAt;
    private OffsetDateTime firstPublishedAt;
    private OffsetDateTime unpublishedAt;
    private OffsetDateTime archivedAt;
    private Integer lockVersion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private UUID createdBy;
    private String createdByType;
    private UUID updatedBy;
    private String updatedByType;
}
