package com.systand.cms.api.page;

import java.util.Map;

public record CreatePageLocaleCommand(
        String locale,
        String title,
        String summary,
        String seoTitle,
        String seoDescription,
        Map<String, Object> metadata) {
}
