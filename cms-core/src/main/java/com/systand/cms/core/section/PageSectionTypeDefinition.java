package com.systand.cms.core.section;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Host-independent, versioned definition of an editable page section type. */
public record PageSectionTypeDefinition(
        String code,
        String scope,
        String label,
        String description,
        String icon,
        String defaultKey,
        String defaultAnchor,
        String rendererKey,
        String handlerKey,
        int schemaVersion,
        int sortOrder,
        Map<String, Object> defaultSettings,
        List<MediaSlot> mediaSlots,
        List<String> requiredAnyOfMediaRoles) {

    public PageSectionTypeDefinition {
        code = requireText(code, "code").toLowerCase(Locale.ROOT);
        scope = requireText(scope, "scope").toUpperCase(Locale.ROOT);
        label = requireText(label, "label");
        defaultKey = requireText(defaultKey, "defaultKey");
        rendererKey = requireText(rendererKey, "rendererKey");
        handlerKey = requireText(handlerKey, "handlerKey");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be at least 1");
        }
        defaultSettings = defaultSettings == null ? Map.of() : Map.copyOf(defaultSettings);
        mediaSlots = mediaSlots == null ? List.of() : List.copyOf(mediaSlots);
        requiredAnyOfMediaRoles = requiredAnyOfMediaRoles == null
                ? List.of()
                : requiredAnyOfMediaRoles.stream()
                    .map(PageSectionTypeDefinition::normalizeRole)
                    .toList();
        for (String requiredRole : requiredAnyOfMediaRoles) {
            if (mediaSlots.stream().noneMatch(slot -> slot.role().equals(requiredRole))) {
                throw new IllegalArgumentException("Required media role is not declared as a slot: " + requiredRole);
            }
        }
    }

    public Map<String, Object> mergeSettings(Map<String, Object> settings) {
        Map<String, Object> merged = new LinkedHashMap<>(defaultSettings);
        if (settings != null) {
            merged.putAll(settings);
        }
        return Map.copyOf(merged);
    }

    public Optional<MediaSlot> findMediaSlot(String fileRole) {
        if (fileRole == null || fileRole.isBlank()) {
            return Optional.empty();
        }
        String normalizedRole = normalizeRole(fileRole);
        return mediaSlots.stream().filter(slot -> slot.role().equals(normalizedRole)).findFirst();
    }

    public record MediaSlot(String role, String label, List<String> acceptedMimeTypes, int maxCount) {

        public MediaSlot {
            role = normalizeRole(role);
            label = requireText(label, "label");
            acceptedMimeTypes = acceptedMimeTypes == null
                    ? List.of()
                    : acceptedMimeTypes.stream()
                        .map(value -> requireText(value, "acceptedMimeType").toLowerCase(Locale.ROOT))
                        .toList();
            if (maxCount < 1) {
                throw new IllegalArgumentException("maxCount must be at least 1");
            }
        }

        public boolean accepts(String mimeType) {
            if (mimeType == null || mimeType.isBlank()) {
                return false;
            }
            String normalizedMimeType = mimeType.trim().toLowerCase(Locale.ROOT);
            return acceptedMimeTypes.stream().anyMatch(accepted -> accepted.endsWith("/*")
                    ? normalizedMimeType.startsWith(accepted.substring(0, accepted.length() - 1))
                    : normalizedMimeType.equals(accepted));
        }
    }

    private static String normalizeRole(String role) {
        return requireText(role, "role").toUpperCase(Locale.ROOT);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
