package com.systand.cms.api.page;

import java.util.Map;

public record UpdatePageSectionCommand(
        String sectionKey,
        String sectionType,
        String adminLabel,
        String anchorId,
        Integer sortOrder,
        Boolean visible,
        Map<String, Object> settings,
        Integer schemaVersion,
        Integer lockVersion) {
}
