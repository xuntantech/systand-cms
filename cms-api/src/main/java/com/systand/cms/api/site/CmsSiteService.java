package com.systand.cms.api.site;

import java.util.List;
import java.util.UUID;

/** Host-independent site application contract. */
public interface CmsSiteService {
    List<CmsSiteVO> getSitesByTenant();

    CmsSiteVO getSite(UUID siteId);

    CmsSiteVO createSite(CmsCreateSiteCommand command);
}
