package com.systand.cms.starter;

import com.systand.cms.api.page.CmsPageLocaleService;
import com.systand.cms.api.page.CmsPageSectionLocaleService;
import com.systand.cms.api.page.CmsPageSectionService;
import com.systand.cms.api.page.CmsPageSectionTypeService;
import com.systand.cms.api.page.CmsPageService;
import com.systand.cms.api.site.CmsSiteService;
import com.systand.cms.web.page.controller.PageController;
import com.systand.cms.web.site.controller.SiteController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CmsWebAutoConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CmsWebAutoConfiguration.class)
            .withBean(CmsSiteService.class, () -> mock(CmsSiteService.class))
            .withBean(CmsPageService.class, () -> mock(CmsPageService.class))
            .withBean(CmsPageLocaleService.class, () -> mock(CmsPageLocaleService.class))
            .withBean(CmsPageSectionService.class, () -> mock(CmsPageSectionService.class))
            .withBean(CmsPageSectionLocaleService.class, () -> mock(CmsPageSectionLocaleService.class))
            .withBean(CmsPageSectionTypeService.class, () -> mock(CmsPageSectionTypeService.class));

    @Test
    void discoversSharedControllersByDefault() {
        runner.run(context -> {
            assertEquals(1, context.getBeanNamesForType(SiteController.class).length);
            assertEquals(1, context.getBeanNamesForType(PageController.class).length);
        });
    }

    @Test
    void canDisableOnlyHttpEndpoints() {
        runner.withPropertyValues("systand.cms.web-enabled=false").run(context -> {
            assertEquals(0, context.getBeanNamesForType(SiteController.class).length);
            assertEquals(0, context.getBeanNamesForType(PageController.class).length);
        });
    }

    @Test
    void canReplaceOnlyTheSharedSiteEndpoint() {
        runner.withPropertyValues("systand.cms.web.site-enabled=false").run(context -> {
            assertEquals(0, context.getBeanNamesForType(SiteController.class).length);
            assertEquals(1, context.getBeanNamesForType(PageController.class).length);
        });
    }
}
