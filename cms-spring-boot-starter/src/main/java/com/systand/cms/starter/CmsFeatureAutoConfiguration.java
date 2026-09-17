package com.systand.cms.starter;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/** Activates reusable CMS services and CMS-owned mappers. */
@AutoConfiguration(after = CmsAutoConfiguration.class)
@ConditionalOnProperty(prefix = "systand.cms", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.systand.cms.application")
@MapperScan(basePackages = "com.systand.cms.persistence.mybatis", annotationClass = Mapper.class)
public class CmsFeatureAutoConfiguration {
}
