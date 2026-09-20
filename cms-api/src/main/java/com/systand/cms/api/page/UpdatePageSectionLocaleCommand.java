package com.systand.cms.api.page;

import java.util.Map;

public record UpdatePageSectionLocaleCommand(
        SectionTranslationStatus translationStatus,
        Map<String, Object> content,
        Integer lockVersion) {
}
