package com.systand.cms.application.page.port;

import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.application.page.model.PageState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsPageRepository {
    List<PageState> findAll(UUID tenantId, UUID siteId, PagePublicationStatus status, PageKind kind, String keyword);
    Optional<PageState> findById(UUID tenantId, UUID siteId, UUID pageId);
    PageState insert(PageState page);
    int update(PageState page, int expectedLockVersion);
}
