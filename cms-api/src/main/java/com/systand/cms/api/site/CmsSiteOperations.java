package com.systand.cms.api.site;

import java.util.List;

/** Replace this bean to customize the shared site endpoint's business behavior. */
public interface CmsSiteOperations {
    List<CmsSiteVO> getSitesByTenant();

    CmsSiteVO createSite(CmsCreateSiteCommand command);
}
