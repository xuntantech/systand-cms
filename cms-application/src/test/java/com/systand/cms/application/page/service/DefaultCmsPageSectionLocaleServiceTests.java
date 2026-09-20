package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.page.CreatePageSectionLocaleCommand;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.SectionTranslationStatus;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageSectionLocaleConverter;
import com.systand.cms.application.page.model.PageSectionLocaleState;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

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

class DefaultCmsPageSectionLocaleServiceTests {
    private CmsPageSectionLocaleRepository localeRepository;
    private CmsPageSectionRepository sectionRepository;
    private CmsPageRepository pageRepository;
    private CmsSiteRepository siteRepository;
    private DefaultCmsPageSectionLocaleService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        localeRepository = mock(CmsPageSectionLocaleRepository.class);
        sectionRepository = mock(CmsPageSectionRepository.class);
        pageRepository = mock(CmsPageRepository.class);
        siteRepository = mock(CmsSiteRepository.class);
        CmsActorProvider actorProvider = mock(CmsActorProvider.class);
        CmsTenantProvider tenantProvider = mock(CmsTenantProvider.class);
        tenantId = UUID.randomUUID();
        when(tenantProvider.requireTenantId()).thenReturn(tenantId);
        when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(UUID.randomUUID()));
        service = new DefaultCmsPageSectionLocaleService(
                localeRepository, sectionRepository, pageRepository, siteRepository,
                actorProvider, tenantProvider,
                Mappers.getMapper(CmsPageSectionLocaleConverter.class));
    }

    @Test
    void createCanonicalizesLocaleAndUsesParentScope() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        stubContext(siteId, pageId, sectionId);
        when(localeRepository.insert(any(PageSectionLocaleState.class))).thenAnswer(invocation -> {
            PageSectionLocaleState state = invocation.getArgument(0);
            state.setId(UUID.randomUUID());
            return state;
        });

        service.createSectionLocale(siteId, pageId, sectionId,
                new CreatePageSectionLocaleCommand(
                        "ZH-cn", SectionTranslationStatus.DRAFT, Map.of("title", "标题")));

        ArgumentCaptor<PageSectionLocaleState> captor =
                ArgumentCaptor.forClass(PageSectionLocaleState.class);
        verify(localeRepository).insert(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
        assertEquals("zh-CN", captor.getValue().getLocale());
    }

    @Test
    void completeContentCannotBeEmpty() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        stubContext(siteId, pageId, sectionId);

        assertThrows(CmsException.class, () -> service.createSectionLocale(
                siteId, pageId, sectionId,
                new CreatePageSectionLocaleCommand(
                        "zh-CN", SectionTranslationStatus.COMPLETE, Map.of())));
    }

    private void stubContext(UUID siteId, UUID pageId, UUID sectionId) {
        CmsSiteState site = new CmsSiteState();
        site.setId(siteId);
        site.setStatus("ACTIVE");
        site.setDefaultLocale("zh-CN");
        site.setEnabledLocales(List.of("zh-CN", "en-US"));
        PageState page = new PageState();
        page.setId(pageId);
        page.setTenantId(tenantId);
        page.setSiteId(siteId);
        page.setPublicationStatus(PagePublicationStatus.DRAFT);
        PageSectionState section = new PageSectionState();
        section.setId(sectionId);
        when(siteRepository.findById(tenantId, siteId)).thenReturn(Optional.of(site));
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));
        when(sectionRepository.findById(tenantId, siteId, pageId, sectionId))
                .thenReturn(Optional.of(section));
    }
}
