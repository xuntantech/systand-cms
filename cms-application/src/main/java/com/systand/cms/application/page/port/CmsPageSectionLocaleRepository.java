package com.systand.cms.application.page.port;

import com.systand.cms.application.page.model.PageSectionLocaleState;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsPageSectionLocaleRepository {
    List<PageSectionLocaleState> findAll(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId);
    Optional<PageSectionLocaleState> findById(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId, UUID localeId);
    PageSectionLocaleState insert(PageSectionLocaleState locale);
    int update(PageSectionLocaleState locale, int expectedLockVersion);
    int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                         UUID actorId, String actorType);
    int softDeleteBySection(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId,
                            OffsetDateTime deletedAt, UUID actorId, String actorType);
}
