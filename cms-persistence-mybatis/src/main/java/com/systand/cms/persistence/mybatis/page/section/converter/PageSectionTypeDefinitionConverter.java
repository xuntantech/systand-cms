package com.systand.cms.persistence.mybatis.page.section.converter;

import com.systand.cms.core.section.PageSectionTypeDefinition;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeCatalogDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Converts the persisted catalog schema into the single core definition. */
public final class PageSectionTypeDefinitionConverter {
    private PageSectionTypeDefinitionConverter() {
    }

    public static PageSectionTypeDefinition from(PageSectionTypeCatalogDO source) {
        Map<String, Object> mediaSchema = source.getMediaSchema() == null
                ? Map.of() : source.getMediaSchema();
        return new PageSectionTypeDefinition(
                source.getCode(), source.getScope(), source.getLabel(),
                source.getDescription(), source.getIcon(), source.getDefaultSectionKey(),
                source.getDefaultAnchor(), source.getRendererKey(), source.getHandlerKey(),
                source.getSchemaVersion(),
                source.getSortOrder() == null ? 0 : source.getSortOrder(),
                source.getDefaultSettings(), parseMediaSlots(mediaSchema.get("slots")),
                parseStrings(mediaSchema.get("requiredAnyOf")));
    }

    private static List<PageSectionTypeDefinition.MediaSlot> parseMediaSlots(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        List<PageSectionTypeDefinition.MediaSlot> slots = new ArrayList<>();
        for (Object item : values) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String role = stringValue(map.get("role"));
            if (role == null) {
                continue;
            }
            String normalizedRole = role.toUpperCase(Locale.ROOT);
            String label = stringValue(map.get("label"));
            int maxCount = map.get("maxCount") instanceof Number number
                    ? Math.max(1, number.intValue()) : 1;
            slots.add(new PageSectionTypeDefinition.MediaSlot(
                    normalizedRole,
                    label == null ? defaultMediaLabel(normalizedRole) : label,
                    parseStrings(map.get("accept")),
                    maxCount));
        }
        return List.copyOf(slots);
    }

    private static List<String> parseStrings(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream().map(PageSectionTypeDefinitionConverter::stringValue)
                .filter(item -> item != null && !item.isBlank()).toList();
    }

    private static String stringValue(Object value) {
        return value instanceof String text && !text.isBlank() ? text.trim() : null;
    }

    private static String defaultMediaLabel(String role) {
        return switch (role) {
            case "BACKGROUND_IMAGE" -> "背景图片";
            case "BACKGROUND_VIDEO" -> "背景视频";
            case "VIDEO_POSTER" -> "视频封面";
            case "MOBILE_IMAGE" -> "移动端图片";
            case "CONTENT_IMAGE" -> "正文配图";
            default -> role;
        };
    }
}
