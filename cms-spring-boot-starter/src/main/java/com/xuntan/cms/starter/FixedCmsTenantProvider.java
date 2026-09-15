package com.xuntan.cms.starter;

import com.xuntan.cms.api.tenant.CmsTenantProvider;

import java.util.Objects;
import java.util.UUID;

/** Tenant provider for an internal deployment that belongs to one real tenant. */
public final class FixedCmsTenantProvider implements CmsTenantProvider {

    private final UUID tenantId;

    public FixedCmsTenantProvider(UUID tenantId) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
    }

    @Override
    public UUID requireTenantId() {
        return tenantId;
    }
}
