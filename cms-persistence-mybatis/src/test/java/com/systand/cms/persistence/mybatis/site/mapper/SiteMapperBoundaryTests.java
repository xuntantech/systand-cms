package com.systand.cms.persistence.mybatis.site.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SiteMapperBoundaryTests {

    @Test
    void tenantScopedMapperDoesNotDeclareBypassMethods() {
        assertEquals(0, SiteMapper.class.getDeclaredMethods().length);
    }

    @Test
    void administrationMethodsExplicitlyBypassTenantInterceptor() throws Exception {
        assertNotNull(SiteAdministrationMapper.class
                .getMethod("selectAllSites")
                .getAnnotation(InterceptorIgnore.class));
        assertNotNull(SiteAdministrationMapper.class
                .getMethod("selectSiteById", UUID.class)
                .getAnnotation(InterceptorIgnore.class));
        assertNotNull(SiteAdministrationMapper.class
                .getMethod("selectSitesByTenantId", UUID.class)
                .getAnnotation(InterceptorIgnore.class));
    }
}
