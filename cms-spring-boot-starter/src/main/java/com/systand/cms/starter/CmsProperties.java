package com.systand.cms.starter;

import com.systand.cms.api.tenant.CmsTenantMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties("systand.cms")
public class CmsProperties {

    private boolean enabled = true;
    private final Tenant tenant = new Tenant();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public static class Tenant {
        private CmsTenantMode mode = CmsTenantMode.CUSTOM;
        private UUID fixedId;

        public CmsTenantMode getMode() {
            return mode;
        }

        public void setMode(CmsTenantMode mode) {
            this.mode = mode;
        }

        public UUID getFixedId() {
            return fixedId;
        }

        public void setFixedId(UUID fixedId) {
            this.fixedId = fixedId;
        }
    }
}
