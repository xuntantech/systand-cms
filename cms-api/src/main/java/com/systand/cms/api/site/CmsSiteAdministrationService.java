package com.systand.cms.api.site;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cross-tenant site administration contract. The host must authorize a trusted
 * platform administrator before invoking this service.
 */
public interface CmsSiteAdministrationService {
    List<CmsSiteVO> getAllSites();
    List<CmsSiteVO> getSitesByTenantId(UUID tenantId);
    Optional<CmsSiteVO> getSiteById(UUID siteId);
    CmsSiteVO createSite(CmsCreateSiteForTenantCommand command);
}
