package com.systand.cms.api.page;

import java.util.Locale;

public enum SectionTranslationStatus {
    DRAFT,
    COMPLETE;

    public static SectionTranslationStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
