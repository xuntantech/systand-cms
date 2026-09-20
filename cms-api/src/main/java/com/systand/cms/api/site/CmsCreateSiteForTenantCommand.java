package com.systand.cms.api.site;

import java.util.List;
import java.util.UUID;

/** Trusted cross-tenant site creation input for host administration use cases. */
public record CmsCreateSiteForTenantCommand(
        UUID tenantId,
        String code,
        String name,
        String defaultLocale,
        List<String> enabledLocales,
        String status) {
}
