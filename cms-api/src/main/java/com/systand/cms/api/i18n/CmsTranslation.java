package com.systand.cms.api.i18n;

import java.util.Map;

/** Host-neutral translation payload for legacy CMS catalogues. */
public record CmsTranslation(String value, Map<String, Object> richValue) {
}
