package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.page.CreatePageLocaleCommand;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.UpdatePageLocaleCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageLocaleConverter;
import com.systand.cms.application.page.model.PageLocaleState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
import com.systand.cms.application.page.port.CmsPageRepository;
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

class DefaultCmsPageLocaleServiceTests {
    private CmsPageLocaleRepository localeRepository;
    private CmsPageRepository pageRepository;
    private CmsSiteRepository siteRepository;
    private CmsTenantProvider tenantProvider;
    private DefaultCmsPageLocaleService service;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        localeRepository = mock(CmsPageLocaleRepository.class);
        pageRepository = mock(CmsPageRepository.class);
        siteRepository = mock(CmsSiteRepository.class);
        CmsActorProvider actorProvider = mock(CmsActorProvider.class);
        tenantProvider = mock(CmsTenantProvider.class);
        tenantId = UUID.randomUUID();
        when(tenantProvider.requireTenantId()).thenReturn(tenantId);
        when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(UUID.randomUUID()));
        service = new DefaultCmsPageLocaleService(
                localeRepository, pageRepository, siteRepository,
                actorProvider, tenantProvider, Mappers.getMapper(CmsPageLocaleConverter.class));
    }

    @Test
    void createUsesParentScopeAndCurrentActor() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        stubPageContext(siteId, pageId, "zh-CN");
        when(localeRepository.insert(any(PageLocaleState.class))).thenAnswer(invocation -> {
            PageLocaleState state = invocation.getArgument(0);
            state.setId(UUID.randomUUID());
            return state;
        });

        service.createPageLocale(siteId, pageId, new CreatePageLocaleCommand(
                "ZH-cn", "标题", null, null, null, Map.of()));

        ArgumentCaptor<PageLocaleState> captor = ArgumentCaptor.forClass(PageLocaleState.class);
        verify(localeRepository).insert(captor.capture());
        assertEquals(tenantId, captor.getValue().getTenantId());
        assertEquals("zh-CN", captor.getValue().getLocale());
    }

    @Test
    void updateRejectsStaleLocaleVersion() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID localeId = UUID.randomUUID();
        stubPageContext(siteId, pageId, "zh-CN");
        PageLocaleState locale = locale(siteId, pageId, localeId, "en-US", 2);
        when(localeRepository.findById(tenantId, siteId, pageId, localeId))
                .thenReturn(Optional.of(locale));

        assertThrows(CmsException.class, () -> service.updatePageLocale(
                siteId, pageId, localeId,
                new UpdatePageLocaleCommand("Title", null, null, null, Map.of(), 1)));
    }

    @Test
    void deleteRejectsSiteDefaultLocale() {
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();
        UUID localeId = UUID.randomUUID();
        stubPageContext(siteId, pageId, "zh-CN");
        when(localeRepository.findById(tenantId, siteId, pageId, localeId))
                .thenReturn(Optional.of(locale(siteId, pageId, localeId, "zh-CN", 1)));

        assertThrows(CmsException.class,
                () -> service.deletePageLocale(siteId, pageId, localeId, 1));
    }

    private void stubPageContext(UUID siteId, UUID pageId, String defaultLocale) {
        CmsSiteState site = new CmsSiteState();
        site.setId(siteId);
        site.setStatus("ACTIVE");
        site.setDefaultLocale(defaultLocale);
        site.setEnabledLocales(List.of("zh-CN", "en-US"));
        PageState page = new PageState();
        page.setId(pageId);
        page.setTenantId(tenantId);
        page.setSiteId(siteId);
        page.setPublicationStatus(PagePublicationStatus.DRAFT);
        when(siteRepository.findById(tenantId, siteId)).thenReturn(Optional.of(site));
        when(pageRepository.findById(tenantId, siteId, pageId)).thenReturn(Optional.of(page));
    }

    private PageLocaleState locale(
            UUID siteId, UUID pageId, UUID localeId, String locale, int lockVersion) {
        PageLocaleState state = new PageLocaleState();
        state.setId(localeId);
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setPageId(pageId);
        state.setLocale(locale);
        state.setLockVersion(lockVersion);
        return state;
    }
}
