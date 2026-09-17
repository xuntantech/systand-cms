package com.systand.cms.api.i18n;

/** Optional host translation lookup; a missing key returns null. */
@FunctionalInterface
public interface CmsTranslationProvider {
    CmsTranslation getTranslation(String locale, String key);
}
