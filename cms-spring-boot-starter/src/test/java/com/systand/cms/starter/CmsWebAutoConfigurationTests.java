package com.systand.cms.starter;

import com.systand.cms.application.page.section.service.PageSectionLocaleService;
import com.systand.cms.application.page.section.service.PageSectionService;
import com.systand.cms.application.page.section.service.PageSectionTypeService;
import com.systand.cms.application.page.service.PageLocaleService;
import com.systand.cms.application.page.service.PageService;
import com.systand.cms.api.site.CmsSiteOperations;
import com.systand.cms.web.page.controller.PageController;
import com.systand.cms.web.site.controller.SiteController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CmsWebAutoConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CmsWebAutoConfiguration.class)
            .withBean(CmsSiteOperations.class, () -> mock(CmsSiteOperations.class))
            .withBean(PageService.class, () -> mock(PageService.class))
            .withBean(PageLocaleService.class, () -> mock(PageLocaleService.class))
            .withBean(PageSectionService.class, () -> mock(PageSectionService.class))
            .withBean(PageSectionLocaleService.class, () -> mock(PageSectionLocaleService.class))
            .withBean(PageSectionTypeService.class, () -> mock(PageSectionTypeService.class));

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
