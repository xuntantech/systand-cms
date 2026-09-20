package com.systand.cms.application.page.service;

import com.systand.cms.api.page.CmsPageSectionTypeService;
import com.systand.cms.api.page.PageSectionTypeVO;
import com.systand.cms.application.page.converter.CmsPageSectionTypeConverter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultCmsPageSectionTypeService implements CmsPageSectionTypeService {
    private final PageSectionTypeDefinitionService definitionService;
    private final CmsPageSectionTypeConverter converter;

    @Override
    public List<PageSectionTypeVO> getAvailableSectionTypes() {
        return converter.toVOs(definitionService.getAvailableDefinitions());
    }

    @Override
    public PageSectionTypeVO getAvailableSectionType(String type, Integer schemaVersion) {
        return converter.toVO(definitionService.requireAvailableDefinition(type, schemaVersion));
    }
}
