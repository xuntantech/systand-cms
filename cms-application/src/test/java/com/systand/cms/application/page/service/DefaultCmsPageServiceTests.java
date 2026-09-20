package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.page.CreatePageCommand;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.PageVO;
import com.systand.cms.api.page.PublishPageCommand;
import com.systand.cms.api.page.SchedulePageCommand;
import com.systand.cms.api.page.UpdatePageCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageConverter;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCmsPageServiceTests {
    private CmsPageRepository pageRepository;
    private CmsPageSectionRepository sectionRepository;
    private CmsSiteRepository siteRepository;
    private CmsActorProvider actorProvider;
    private CmsMediaPort mediaPort;
    private CmsTenantProvider tenantProvider;
    private PageSectionTypeDefinitionService typeService;
    private DefaultCmsPageService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        pageRepository = mock(CmsPageRepository.class);
        sectionRepository = mock(CmsPageSectionRepository.class);
        siteRepository = mock(CmsSiteRepository.class);
        actorProvider = mock(CmsActorProvider.class);
        mediaPort = mock(CmsMediaPort.class);
        tenantProvider = mock(CmsTenantProvider.class);
        typeService = mock(PageSectionTypeDefinitionService.class);
        tenantId = UUID.randomUUID();
        when(tenantProvider.requireTenantId()).thenReturn(tenantId);
        when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(UUID.randomUUID()));
        service = new DefaultCmsPageService(
                pageRepository,
                mock(CmsPageLocaleRepository.class),
                sectionRepository,
                mock(CmsPageSectionLocaleRepository.class),
                siteRepository,
                actorProvider,
                mediaPort,
                tenantProvider,
                typeService,
                Mappers.getMapper(CmsPageConverter.class));
    }

    @Test
    void createPageAlwaysStartsAsDraft() {
        UUID siteId = UUID.randomUUID();
        when(siteRepository.existsActive(tenantId, siteId)).thenReturn(true);
        when(pageRepository.insert(any(PageState.class))).thenAnswer(invocation -> {
            PageState page = invocation.getArgument(0);
            page.setId(UUID.randomUUID());
            return page;
        });

        PageVO result = service.createPage(siteId, new CreatePageCommand(
                "about", "/about", PageKind.STANDARD, "default", false, 0, Map.of()));

        assertEquals(PagePublicationStatus.DRAFT, result.publicationStatus());
        ArgumentCaptor<PageState> captor = ArgumentCaptor.forClass(PageState.class);
        verify(pageRepository).insert(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
    }

    @Test
    void updateRejectsStaleVersion() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        PageState page = page(tenantId, siteId, pageId, PagePublicationStatus.DRAFT, 2);
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));

        assertThrows(CmsException.class, () -> service.updatePage(siteId, pageId,
                new UpdatePageCommand("about", "/about", PageKind.STANDARD,
                        "default", false, 0, Map.of(), 1)));
    }

    @Test
    void scheduleRejectsInvalidPublicationWindow() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        PageState page = page(tenantId, siteId, pageId, PagePublicationStatus.DRAFT, 1);
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));
        OffsetDateTime publishAt = OffsetDateTime.now().plusDays(2);

        assertThrows(CmsException.class, () -> service.schedulePage(siteId, pageId,
                new SchedulePageCommand(publishAt, publishAt.minusHours(1), 1)));
    }

    @Test
    void publishRejectsHeroWithoutBackgroundMedia() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        PageState page = page(tenantId, siteId, pageId, PagePublicationStatus.DRAFT, 1);
        PageSectionState section = new PageSectionState();
        section.setId(sectionId);
        section.setSectionKey("hero");
        section.setSectionType("hero");
        section.setSchemaVersion(1);
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));
        when(sectionRepository.findAll(tenantId, siteId, pageId, true, null))
                .thenReturn(List.of(section));
        when(typeService.requireAvailableDefinition("hero", 1)).thenReturn(heroDefinition());
        when(mediaPort.findBindings(tenantId, "PAGE_SECTION", sectionId, null)).thenReturn(List.of());

        assertThrows(CmsException.class, () -> service.publishPage(
                siteId, pageId, new PublishPageCommand(null, 1)));
    }

    private PageState page(UUID tenantId, UUID siteId, UUID pageId,
                           PagePublicationStatus status, int lockVersion) {
        PageState page = new PageState();
        page.setTenantId(tenantId);
        page.setSiteId(siteId);
        page.setId(pageId);
        page.setPublicationStatus(status);
        page.setLockVersion(lockVersion);
        return page;
    }

    private PageSectionTypeDefinition heroDefinition() {
        PageSectionTypeDefinition.MediaSlot slot = new PageSectionTypeDefinition.MediaSlot(
                "BACKGROUND_IMAGE", "背景图片", List.of("image/*"), 1);
        return new PageSectionTypeDefinition(
                "hero", "BUILT_IN", "Hero", null, null,
                "hero", null, "hero", "hero", 1, 0,
                Map.of(), List.of(slot), List.of("BACKGROUND_IMAGE"));
    }
}
