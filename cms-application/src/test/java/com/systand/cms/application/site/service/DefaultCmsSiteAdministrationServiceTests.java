package com.systand.cms.application.site.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.site.CmsCreateSiteForTenantCommand;
import com.systand.cms.application.site.converter.CmsSiteConverter;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteAdministrationRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCmsSiteAdministrationServiceTests {

    @Test
    void createsSiteForExplicitTenantUsingTrustedActor() {
        CmsSiteAdministrationRepository repository = mock(CmsSiteAdministrationRepository.class);
        CmsActorProvider actors = mock(CmsActorProvider.class);
        var converter = Mappers.getMapper(CmsSiteConverter.class);
        var service = new DefaultCmsSiteAdministrationService(
                repository, actors, converter, new CmsSiteFactory());
        UUID tenantId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(actors.requireActor()).thenReturn(CmsActor.tenantUser(actorId));
        when(repository.insert(any(CmsSiteState.class))).thenAnswer(invocation -> {
            CmsSiteState site = invocation.getArgument(0);
            site.setId(UUID.randomUUID());
            return site;
        });

        var result = service.createSite(new CmsCreateSiteForTenantCommand(
                tenantId, "corporate", "Corporate", null, List.of(), null));

        assertEquals(tenantId, result.tenantId());
        assertEquals(actorId, result.createdBy());
        assertEquals("zh-CN", result.defaultLocale());
        assertEquals("ACTIVE", result.status());
        verify(repository).insert(any(CmsSiteState.class));
    }
}
