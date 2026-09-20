package com.systand.cms.api.page;

import java.util.Map;

public record UpdatePageCommand(
        String code,
        String routePath,
        PageKind pageKind,
        String templateCode,
        Boolean home,
        Integer sortOrder,
        Map<String, Object> layoutSettings,
        Integer lockVersion) {
}
