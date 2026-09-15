package com.xuntan.cms.api.page;

import java.util.Locale;

public enum PageKind {
    STANDARD,
    LANDING,
    LISTING,
    SYSTEM;

    public static PageKind fromValue(String value) {
        return value == null ? null : valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }
}
