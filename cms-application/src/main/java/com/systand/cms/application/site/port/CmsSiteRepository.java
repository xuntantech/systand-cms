package com.systand.cms.application.site.port;

import com.systand.cms.application.site.model.CmsSiteState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsSiteRepository {
    List<CmsSiteState> findAllByTenantId(UUID tenantId);
    Optional<CmsSiteState> findById(UUID tenantId, UUID siteId);
    boolean existsActive(UUID tenantId, UUID siteId);
    CmsSiteState insert(CmsSiteState site);
}
