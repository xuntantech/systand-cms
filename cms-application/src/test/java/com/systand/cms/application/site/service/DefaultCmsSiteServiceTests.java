package com.systand.cms.application.site.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.site.CmsCreateSiteCommand;
import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.site.converter.CmsSiteConverter;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCmsSiteServiceTests {
    @Test
    void createsSiteUsingTrustedTenantAndActor() {
        CmsSiteRepository repository = mock(CmsSiteRepository.class);
        CmsActorProvider actors = mock(CmsActorProvider.class);
        CmsTenantProvider tenants = mock(CmsTenantProvider.class);
        CmsSiteConverter converter = Mappers.getMapper(CmsSiteConverter.class);
        DefaultCmsSiteService service = new DefaultCmsSiteService(
                repository, actors, tenants, converter, new CmsSiteFactory());

        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(tenants.requireTenantId()).thenReturn(tenantId);
        when(actors.requireActor()).thenReturn(CmsActor.tenantUser(actorId));
        when(repository.insert(any(CmsSiteState.class))).thenAnswer(invocation -> {
            CmsSiteState site = invocation.getArgument(0);
            site.setId(UUID.randomUUID());
            return site;
        });

        CmsSiteVO result = service.createSite(
                new CmsCreateSiteCommand("home", "Home", "zh-CN", List.of("zh-CN")));

        assertEquals(tenantId, result.tenantId());
        assertEquals(actorId, result.createdBy());
        verify(repository).insert(any(CmsSiteState.class));
    }
}
