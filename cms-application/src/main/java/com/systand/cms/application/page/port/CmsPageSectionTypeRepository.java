package com.systand.cms.application.page.port;

import com.systand.cms.core.section.PageSectionTypeDefinition;

import java.util.List;
import java.util.UUID;

public interface CmsPageSectionTypeRepository {
    void initializeBuiltIns(UUID tenantId);
    List<PageSectionTypeDefinition> findAvailable(UUID tenantId, String code, Integer schemaVersion);
}
