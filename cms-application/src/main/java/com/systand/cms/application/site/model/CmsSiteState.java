package com.systand.cms.application.site.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CmsSiteState {
    private UUID id;
    private UUID tenantId;
    private String code;
    private String name;
    private String defaultLocale;
    private List<String> enabledLocales;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
