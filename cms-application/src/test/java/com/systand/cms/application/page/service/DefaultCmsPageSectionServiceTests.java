package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.page.CreatePageSectionCommand;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.UpdatePageSectionCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageSectionConverter;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCmsPageSectionServiceTests {
    private CmsPageSectionRepository sectionRepository;
    private CmsPageSectionLocaleRepository localeRepository;
    private CmsPageRepository pageRepository;
    private CmsSiteRepository siteRepository;
    private PageSectionTypeDefinitionService typeService;
    private DefaultCmsPageSectionService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        sectionRepository = mock(CmsPageSectionRepository.class);
        localeRepository = mock(CmsPageSectionLocaleRepository.class);
        pageRepository = mock(CmsPageRepository.class);
        siteRepository = mock(CmsSiteRepository.class);
        typeService = mock(PageSectionTypeDefinitionService.class);
        CmsActorProvider actorProvider = mock(CmsActorProvider.class);
        CmsTenantProvider tenantProvider = mock(CmsTenantProvider.class);
        tenantId = UUID.randomUUID();
        when(tenantProvider.requireTenantId()).thenReturn(tenantId);
        when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(UUID.randomUUID()));
        service = new DefaultCmsPageSectionService(
                sectionRepository, localeRepository, pageRepository, siteRepository,
                actorProvider, mock(CmsMediaPort.class), tenantProvider, typeService,
                Mappers.getMapper(CmsPageSectionConverter.class));
    }

    @Test
    void createUsesPageScopeAndCurrentActor() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        stubPage(siteId, pageId);
        when(typeService.requireAvailableDefinition("hero", 1)).thenReturn(definition());
        when(sectionRepository.insert(any(PageSectionState.class))).thenAnswer(invocation -> {
            PageSectionState state = invocation.getArgument(0);
            state.setId(UUID.randomUUID());
            return state;
        });

        service.createSection(siteId, pageId, new CreatePageSectionCommand(
                "hero", "hero", null, null, 0, true, Map.of(), 1));

        ArgumentCaptor<PageSectionState> captor = ArgumentCaptor.forClass(PageSectionState.class);
        verify(sectionRepository).insert(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
        assertEquals(pageId, captor.getValue().getPageId());
    }

    @Test
    void createRejectsUnknownSectionType() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        stubPage(siteId, pageId);
        when(typeService.requireAvailableDefinition(anyString(), any(Integer.class)))
                .thenThrow(new CmsException(com.systand.cms.api.error.CmsErrorCode.INVALID_ARGUMENT, "unknown"));

        assertThrows(CmsException.class, () -> service.createSection(siteId, pageId,
                new CreatePageSectionCommand("hero", "unknown", null, null,
                        0, true, Map.of(), 1)));
    }

    @Test
    void updateRejectsStaleVersion() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        stubPage(siteId, pageId);
        when(sectionRepository.findById(tenantId, siteId, pageId, sectionId))
                .thenReturn(Optional.of(section(siteId, pageId, sectionId, 2)));

        assertThrows(CmsException.class, () -> service.updateSection(siteId, pageId, sectionId,
                new UpdatePageSectionCommand("hero", "hero", null, null,
                        0, true, Map.of(), 1, 1)));
    }

    @Test
    void deleteSoftDeletesChildLocales() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        stubPage(siteId, pageId);
        when(sectionRepository.findById(tenantId, siteId, pageId, sectionId))
                .thenReturn(Optional.of(section(siteId, pageId, sectionId, 1)));
        when(sectionRepository.update(any(PageSectionState.class), org.mockito.ArgumentMatchers.eq(1)))
                .thenReturn(1);

        service.deleteSection(siteId, pageId, sectionId, 1);

        verify(localeRepository).softDeleteBySection(
                org.mockito.ArgumentMatchers.eq(tenantId),
                org.mockito.ArgumentMatchers.eq(siteId),
                org.mockito.ArgumentMatchers.eq(pageId),
                org.mockito.ArgumentMatchers.eq(sectionId),
                any(OffsetDateTime.class), any(), anyString());
    }

    private void stubPage(UUID siteId, UUID pageId) {
        PageState page = new PageState();
        page.setId(pageId);
        page.setTenantId(tenantId);
        page.setSiteId(siteId);
        page.setPublicationStatus(PagePublicationStatus.DRAFT);
        when(siteRepository.existsActive(tenantId, siteId)).thenReturn(true);
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));
    }

    private PageSectionState section(UUID siteId, UUID pageId, UUID sectionId, int lockVersion) {
        PageSectionState state = new PageSectionState();
        state.setId(sectionId);
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setPageId(pageId);
        state.setSectionKey("hero");
        state.setSectionType("hero");
        state.setLockVersion(lockVersion);
        return state;
    }

    private PageSectionTypeDefinition definition() {
        return new PageSectionTypeDefinition(
                "hero", "BUILT_IN", "Hero", null, null,
                "hero", null, "hero", "hero", 1, 0,
                Map.of("theme", "light"), List.of(), List.of());
    }
}
