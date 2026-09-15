package com.xuntan.cms.api.page;

import java.util.Locale;

public enum PagePublicationStatus {
    DRAFT,
    IN_REVIEW,
    SCHEDULED,
    PUBLISHED,
    UNPUBLISHED,
    ARCHIVED;

    public static PagePublicationStatus fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
