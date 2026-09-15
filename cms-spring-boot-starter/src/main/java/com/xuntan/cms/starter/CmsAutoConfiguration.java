package com.xuntan.cms.starter;

import com.xuntan.cms.api.tenant.CmsTenantMode;
import com.xuntan.cms.api.tenant.CmsTenantProvider;
import com.xuntan.cms.core.page.PagePublicationPolicy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(CmsProperties.class)
@ConditionalOnProperty(prefix = "systand.cms", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CmsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    PagePublicationPolicy pagePublicationPolicy() {
        return new PagePublicationPolicy();
    }

    @Bean
    @ConditionalOnMissingBean(CmsTenantProvider.class)
    @ConditionalOnProperty(prefix = "systand.cms.tenant", name = "mode", havingValue = "fixed")
    FixedCmsTenantProvider fixedCmsTenantProvider(CmsProperties properties) {
        if (properties.getTenant().getFixedId() == null) {
            throw new IllegalStateException(
                    "systand.cms.tenant.fixed-id is required when tenant mode is FIXED");
        }
        return new FixedCmsTenantProvider(properties.getTenant().getFixedId());
    }

    @Bean
    SmartInitializingSingleton cmsTenantProviderValidator(
            CmsProperties properties, ObjectProvider<CmsTenantProvider> providers) {
        return () -> {
            CmsTenantProvider provider = providers.getIfUnique();
            if (provider == null) {
                throw new IllegalStateException(
                        "CMS requires exactly one CmsTenantProvider; configure FIXED mode or provide a custom bean");
            }
            if (provider.requireTenantId() == null) {
                throw new IllegalStateException("CmsTenantProvider must not return null");
            }
            if (properties.getTenant().getMode() == CmsTenantMode.FIXED
                    && !(provider instanceof FixedCmsTenantProvider)) {
                throw new IllegalStateException(
                        "FIXED tenant mode must use the starter-managed FixedCmsTenantProvider");
            }
        };
    }
}
