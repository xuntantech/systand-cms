package com.systand.cms.api.site;

import java.util.List;

/** Host-independent site creation input; tenant and actor come from providers. */
public record CmsCreateSiteCommand(
        String code, String name, String defaultLocale, List<String> enabledLocales) {
}
