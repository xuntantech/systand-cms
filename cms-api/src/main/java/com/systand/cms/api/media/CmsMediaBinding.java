package com.systand.cms.api.media;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Host-neutral representation of a CMS media binding. */
public record CmsMediaBinding(
        UUID id,
        UUID fileId,
        String resourceType,
        UUID resourceId,
        String role,
        int sortOrder,
        boolean primary,
        Map<String, Object> metadata) {

    public CmsMediaBinding {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(fileId, "fileId");
        Objects.requireNonNull(resourceId, "resourceId");
        resourceType = requireText(resourceType, "resourceType");
        role = requireText(role, "role");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
