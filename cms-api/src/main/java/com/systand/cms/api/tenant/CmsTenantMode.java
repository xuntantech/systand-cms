package com.systand.cms.api.tenant;

/** Supported tenant resolution modes for a CMS host. */
public enum CmsTenantMode {
    /** One real tenant is configured for the whole internal deployment. */
    FIXED,
    /** The host supplies a trusted request-aware {@link CmsTenantProvider}. */
    CUSTOM
}
