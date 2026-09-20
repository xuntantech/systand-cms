package com.systand.cms.api.page;

import java.util.Map;

public record CreatePageSectionLocaleCommand(
        String locale,
        SectionTranslationStatus translationStatus,
        Map<String, Object> content) {
}
