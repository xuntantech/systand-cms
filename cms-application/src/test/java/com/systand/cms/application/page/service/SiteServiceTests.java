package com.systand.cms.application.site.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SiteServiceTests {
    @Test
    void createsSiteUsingTrustedTenantAndActor() {
        SiteMapper mapper = mock(SiteMapper.class);
        CmsActorProvider actors = mock(CmsActorProvider.class);
        CmsTenantProvider tenants = mock(CmsTenantProvider.class);
        SiteService service = new SiteService(mapper, actors, tenants);

        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(tenants.requireTenantId()).thenReturn(tenantId);
        when(actors.requireActor()).thenReturn(CmsActor.tenantUser(actorId));
        AtomicReference<SiteDO> insertedSite = new AtomicReference<>();
        when(mapper.insert(any(SiteDO.class))).thenAnswer(invocation -> {
            SiteDO site = invocation.getArgument(0);
            site.setId(UUID.randomUUID());
            insertedSite.set(site);
            return 1;
        });
        when(mapper.selectOne(any(Wrapper.class))).thenAnswer(invocation -> insertedSite.get());
        SiteDO result = service.createSite(new CreateSiteRequest("home", "Home", "zh-CN", List.of("zh-CN")));
        assertEquals(tenantId, result.getTenantId());
        assertEquals(actorId, result.getCreatedBy());
        verify(mapper).insert(any(SiteDO.class));
    }
}
