package com.systand.cms.starter;

import com.systand.cms.api.tenant.CmsTenantMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties("systand.cms")
public class CmsProperties {

    private boolean enabled = true;
    private boolean webEnabled = true;
    private String apiPrefix = "/v1/cms";
    private final Tenant tenant = new Tenant();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isWebEnabled() {
        return webEnabled;
    }

    public void setWebEnabled(boolean webEnabled) {
        this.webEnabled = webEnabled;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        if (apiPrefix == null || !apiPrefix.matches("/[a-zA-Z0-9/_-]+")
                || apiPrefix.endsWith("/") || apiPrefix.contains("//")) {
            throw new IllegalArgumentException("systand.cms.api-prefix must be an absolute path without a trailing slash");
        }
        this.apiPrefix = apiPrefix;
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
