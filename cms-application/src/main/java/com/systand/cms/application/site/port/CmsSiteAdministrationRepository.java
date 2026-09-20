package com.systand.cms.application.site.port;

import com.systand.cms.application.site.model.CmsSiteState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CmsSiteAdministrationRepository {
    List<CmsSiteState> findAll();
    List<CmsSiteState> findAllByTenantId(UUID tenantId);
    Optional<CmsSiteState> findById(UUID siteId);
    CmsSiteState insert(CmsSiteState site);
}
