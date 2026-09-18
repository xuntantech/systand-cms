package com.systand.cms.starter;

import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.site.CmsSiteOperations;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.site.service.DefaultCmsSiteOperations;
import com.systand.cms.application.site.service.CmsSiteVOMapper;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/** Activates reusable CMS services and CMS-owned mappers. */
@AutoConfiguration(after = CmsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "systand.cms", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.systand.cms.application")
@MapperScan(basePackages = "com.systand.cms.persistence.mybatis", annotationClass = Mapper.class)
public class CmsFeatureAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean(CmsSiteOperations.class)
	CmsSiteOperations cmsSiteOperations(SiteMapper siteMapper,
			CmsActorProvider actorProvider, CmsTenantProvider tenantProvider,
			CmsSiteVOMapper siteVOMapper) {
		return new DefaultCmsSiteOperations(siteMapper, actorProvider, tenantProvider, siteVOMapper);
	}
}
