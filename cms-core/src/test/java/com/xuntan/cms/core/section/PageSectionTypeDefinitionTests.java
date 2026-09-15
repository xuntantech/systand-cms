package com.xuntan.cms.core.section;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageSectionTypeDefinitionTests {

    @Test
    void normalizesMediaRolesAndMatchesMimeWildcards() {
        PageSectionTypeDefinition definition = definition(List.of("hero_image"));

        PageSectionTypeDefinition.MediaSlot slot = definition.findMediaSlot("hero_image").orElseThrow();
        assertTrue(slot.accepts("image/webp"));
        assertFalse(slot.accepts("video/mp4"));
        assertEquals("dark", definition.mergeSettings(Map.of("theme", "dark")).get("theme"));
    }

    @Test
    void rejectsRequiredRoleThatHasNoSlot() {
        assertThrows(IllegalArgumentException.class, () -> definition(List.of("video")));
    }

    private static PageSectionTypeDefinition definition(List<String> requiredRoles) {
        return new PageSectionTypeDefinition(
                "hero",
                "global",
                "Hero",
                null,
                null,
                "hero",
                null,
                "hero",
                "default",
                1,
                0,
                Map.of("theme", "light"),
                List.of(new PageSectionTypeDefinition.MediaSlot(
                        "hero_image", "Hero image", List.of("image/*"), 1)),
                requiredRoles);
    }
}
