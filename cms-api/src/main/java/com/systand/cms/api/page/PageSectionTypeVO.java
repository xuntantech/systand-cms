package com.systand.cms.api.page;

import java.util.List;
import java.util.Map;

public record PageSectionTypeVO(
        String type,
        String scope,
        Integer schemaVersion,
        String label,
        String description,
        String icon,
        String defaultKey,
        String defaultAnchor,
        String rendererKey,
        Map<String, Object> defaultSettings,
        List<MediaSlotVO> mediaSlots,
        List<String> requiredAnyOfMediaRoles) {

    public record MediaSlotVO(
            String role,
            String label,
            List<String> acceptedMimeTypes,
            Integer maxCount) {
    }
}
