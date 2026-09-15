package com.systand.cms.api.media;

import java.util.Objects;
import java.util.UUID;

/** Minimal media metadata required by CMS domain rules. */
public record CmsMediaFile(UUID id, String mimeType, long size) {

    public CmsMediaFile {
        Objects.requireNonNull(id, "id");
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType must not be blank");
        }
        mimeType = mimeType.trim().toLowerCase();
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
    }
}
