package com.systand.cms.starter;

import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.page.CmsPageLocaleService;
import com.systand.cms.api.page.CmsPageSectionLocaleService;
import com.systand.cms.api.page.CmsPageSectionService;
import com.systand.cms.api.page.CmsPageSectionTypeService;
import com.systand.cms.api.page.CmsPageService;
import com.systand.cms.api.site.CmsSiteService;
import com.systand.cms.api.site.CmsSiteAdministrationService;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageConverter;
import com.systand.cms.application.page.converter.CmsPageLocaleConverter;
import com.systand.cms.application.page.converter.CmsPageSectionConverter;
import com.systand.cms.application.page.converter.CmsPageSectionLocaleConverter;
import com.systand.cms.application.page.converter.CmsPageSectionTypeConverter;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.page.port.CmsPageSectionTypeRepository;
import com.systand.cms.application.page.service.DefaultCmsPageLocaleService;
import com.systand.cms.application.page.service.DefaultCmsPageSectionLocaleService;
import com.systand.cms.application.page.service.DefaultCmsPageSectionService;
import com.systand.cms.application.page.service.DefaultCmsPageSectionTypeService;
import com.systand.cms.application.page.service.DefaultCmsPageService;
import com.systand.cms.application.page.service.DefaultPageSectionTypeDefinitionService;
import com.systand.cms.application.page.service.PageSectionTypeDefinitionService;
import com.systand.cms.application.site.converter.CmsSiteConverter;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.application.site.port.CmsSiteAdministrationRepository;
import com.systand.cms.application.site.service.DefaultCmsSiteAdministrationService;
import com.systand.cms.application.site.service.DefaultCmsSiteService;
import com.systand.cms.application.site.service.CmsSiteFactory;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration(after = CmsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "systand.cms", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = {
        "com.systand.cms.application",
        "com.systand.cms.persistence.mybatis"
})
@MapperScan(basePackages = "com.systand.cms.persistence.mybatis", annotationClass = Mapper.class)
public class CmsFeatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CmsSiteFactory cmsSiteFactory() {
        return new CmsSiteFactory();
    }

    @Bean
    @ConditionalOnMissingBean(CmsSiteService.class)
    CmsSiteService cmsSiteService(
            CmsSiteRepository repository,
            CmsActorProvider actorProvider,
            CmsTenantProvider tenantProvider,
            CmsSiteConverter converter,
            CmsSiteFactory siteFactory) {
        return new DefaultCmsSiteService(
                repository, actorProvider, tenantProvider, converter, siteFactory);
    }

    @Bean
    @ConditionalOnMissingBean(CmsSiteAdministrationService.class)
    CmsSiteAdministrationService cmsSiteAdministrationService(
            CmsSiteAdministrationRepository repository,
            CmsActorProvider actorProvider,
            CmsSiteConverter converter,
            CmsSiteFactory siteFactory) {
        return new DefaultCmsSiteAdministrationService(
                repository, actorProvider, converter, siteFactory);
    }

    @Bean
    @ConditionalOnMissingBean(PageSectionTypeDefinitionService.class)
    PageSectionTypeDefinitionService pageSectionTypeDefinitionService(
            CmsPageSectionTypeRepository repository,
            CmsTenantProvider tenantProvider) {
        return new DefaultPageSectionTypeDefinitionService(repository, tenantProvider);
    }

    @Bean
    @ConditionalOnMissingBean(CmsPageSectionTypeService.class)
    CmsPageSectionTypeService cmsPageSectionTypeService(
            PageSectionTypeDefinitionService definitionService,
            CmsPageSectionTypeConverter converter) {
        return new DefaultCmsPageSectionTypeService(definitionService, converter);
    }

    @Bean
    @ConditionalOnMissingBean(CmsPageService.class)
    CmsPageService cmsPageService(
            CmsPageRepository pageRepository,
            CmsPageLocaleRepository pageLocaleRepository,
            CmsPageSectionRepository sectionRepository,
            CmsPageSectionLocaleRepository sectionLocaleRepository,
            CmsSiteRepository siteRepository,
            CmsActorProvider actorProvider,
            CmsMediaPort mediaPort,
            CmsTenantProvider tenantProvider,
            PageSectionTypeDefinitionService sectionTypeService,
            CmsPageConverter converter) {
        return new DefaultCmsPageService(pageRepository, pageLocaleRepository,
                sectionRepository, sectionLocaleRepository, siteRepository,
                actorProvider, mediaPort, tenantProvider, sectionTypeService, converter);
    }

    @Bean
    @ConditionalOnMissingBean(CmsPageLocaleService.class)
    CmsPageLocaleService cmsPageLocaleService(
            CmsPageLocaleRepository localeRepository,
            CmsPageRepository pageRepository,
            CmsSiteRepository siteRepository,
            CmsActorProvider actorProvider,
            CmsTenantProvider tenantProvider,
            CmsPageLocaleConverter converter) {
        return new DefaultCmsPageLocaleService(localeRepository, pageRepository,
                siteRepository, actorProvider, tenantProvider, converter);
    }

    @Bean
    @ConditionalOnMissingBean(CmsPageSectionService.class)
    CmsPageSectionService cmsPageSectionService(
            CmsPageSectionRepository sectionRepository,
            CmsPageSectionLocaleRepository localeRepository,
            CmsPageRepository pageRepository,
            CmsSiteRepository siteRepository,
            CmsActorProvider actorProvider,
            CmsMediaPort mediaPort,
            CmsTenantProvider tenantProvider,
            PageSectionTypeDefinitionService sectionTypeService,
            CmsPageSectionConverter converter) {
        return new DefaultCmsPageSectionService(sectionRepository, localeRepository,
                pageRepository, siteRepository, actorProvider, mediaPort,
                tenantProvider, sectionTypeService, converter);
    }

    @Bean
    @ConditionalOnMissingBean(CmsPageSectionLocaleService.class)
    CmsPageSectionLocaleService cmsPageSectionLocaleService(
            CmsPageSectionLocaleRepository localeRepository,
            CmsPageSectionRepository sectionRepository,
            CmsPageRepository pageRepository,
            CmsSiteRepository siteRepository,
            CmsActorProvider actorProvider,
            CmsTenantProvider tenantProvider,
            CmsPageSectionLocaleConverter converter) {
        return new DefaultCmsPageSectionLocaleService(localeRepository, sectionRepository,
                pageRepository, siteRepository, actorProvider, tenantProvider, converter);
    }
}
