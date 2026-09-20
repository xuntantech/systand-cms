package com.systand.cms.api.page;

import java.util.List;

public interface CmsPageSectionTypeService {
    List<PageSectionTypeVO> getAvailableSectionTypes();
    PageSectionTypeVO getAvailableSectionType(String type, Integer schemaVersion);
}
