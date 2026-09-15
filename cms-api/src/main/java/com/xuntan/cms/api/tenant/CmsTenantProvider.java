package com.xuntan.cms.api.tenant;

import java.util.UUID;

/** Supplies the trusted tenant for the current CMS operation. */
@FunctionalInterface
public interface CmsTenantProvider {

    /**
     * Returns the current tenant. Implementations must fail closed when no trusted
     * tenant is available; returning {@code null} is not permitted.
     */
    UUID requireTenantId();
}
