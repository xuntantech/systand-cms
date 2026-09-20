package com.systand.cms.application.page.service;

import com.systand.cms.core.section.PageSectionTypeDefinition;

import java.util.List;

public interface PageSectionTypeDefinitionService {
    List<PageSectionTypeDefinition> getAvailableDefinitions();
    PageSectionTypeDefinition requireAvailableDefinition(String code, Integer schemaVersion);
}
