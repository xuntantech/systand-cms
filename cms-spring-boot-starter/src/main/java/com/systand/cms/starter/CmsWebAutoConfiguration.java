package com.systand.cms.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

/** Exposes the shared CMS HTTP API when enabled by the host. */
@AutoConfiguration(after = CmsFeatureAutoConfiguration.class)
@ConditionalOnProperty(prefix = "systand.cms", name = {"enabled", "web-enabled"},
        havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "com.systand.cms.web")
public class CmsWebAutoConfiguration {
}
