package com.systand.cms.starter;

import com.systand.cms.api.tenant.CmsTenantMode;
import com.systand.cms.api.tenant.CmsTenantProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CmsAutoConfigurationTests {

    private final CmsAutoConfiguration configuration = new CmsAutoConfiguration();

    @Test
    void configuresFixedTenantAndCorePolicy() {
        UUID tenantId = UUID.fromString("d59a2aa6-39ee-46d0-ad17-02145379168b");
        CmsProperties properties = fixedProperties(tenantId);

        FixedCmsTenantProvider provider = configuration.fixedCmsTenantProvider(properties);

        assertEquals(tenantId, provider.requireTenantId());
        assertNotNull(configuration.pagePublicationPolicy());
        validate(properties, provider);
    }

    @Test
    void rejectsFixedModeWithoutConfiguredTenant() {
        CmsProperties properties = fixedProperties(null);

        assertThrows(
                IllegalStateException.class,
                () -> configuration.fixedCmsTenantProvider(properties));
    }

    @Test
    void failsClosedWhenCustomModeHasNoProvider() {
        CmsProperties properties = new CmsProperties();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        SmartInitializingSingleton validator = configuration.cmsTenantProviderValidator(
                properties, beanFactory.getBeanProvider(CmsTenantProvider.class));

        assertThrows(IllegalStateException.class, validator::afterSingletonsInstantiated);
    }

    @Test
    void acceptsHostProvidedTenantProvider() {
        UUID tenantId = UUID.fromString("b06ee121-a3b3-4f1a-87ba-0252c4a71bd7");
        CmsProperties properties = new CmsProperties();

        validate(properties, () -> tenantId);
    }

    private void validate(CmsProperties properties, CmsTenantProvider provider) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("cmsTenantProvider", provider);
        configuration.cmsTenantProviderValidator(
                        properties, beanFactory.getBeanProvider(CmsTenantProvider.class))
                .afterSingletonsInstantiated();
    }

    private static CmsProperties fixedProperties(UUID tenantId) {
        CmsProperties properties = new CmsProperties();
        properties.getTenant().setMode(CmsTenantMode.FIXED);
        properties.getTenant().setFixedId(tenantId);
        return properties;
    }
}
