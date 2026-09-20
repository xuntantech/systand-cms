package com.systand.cms.api.page;

import java.util.Map;

public record CreatePageSectionCommand(
        String sectionKey,
        String sectionType,
        String adminLabel,
        String anchorId,
        Integer sortOrder,
        Boolean visible,
        Map<String, Object> settings,
        Integer schemaVersion) {
}
