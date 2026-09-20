package com.systand.cms.application.page.port;

import com.systand.cms.application.page.model.PageSectionState;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsPageSectionRepository {
    List<PageSectionState> findAll(UUID tenantId, UUID siteId, UUID pageId, Boolean visible, String sectionType);
    Optional<PageSectionState> findById(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId);
    PageSectionState insert(PageSectionState section);
    int update(PageSectionState section, int expectedLockVersion);
    int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                         UUID actorId, String actorType);
}
