package com.systand.cms.application.page.port;

import com.systand.cms.application.page.model.PageLocaleState;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsPageLocaleRepository {
    List<PageLocaleState> findAll(UUID tenantId, UUID siteId, UUID pageId);
    Optional<PageLocaleState> findById(UUID tenantId, UUID siteId, UUID pageId, UUID localeId);
    PageLocaleState insert(PageLocaleState locale);
    int update(PageLocaleState locale, int expectedLockVersion);
    int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                         UUID actorId, String actorType);
}
