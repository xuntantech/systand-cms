package com.systand.cms.api.page;

import java.util.List;
import java.util.UUID;

public interface CmsPageSectionService {
    List<PageSectionVO> getSections(UUID siteId, UUID pageId, Boolean visible, String sectionType);
    PageSectionVO getSection(UUID siteId, UUID pageId, UUID sectionId);
    PageSectionVO createSection(UUID siteId, UUID pageId, CreatePageSectionCommand command);
    PageSectionVO updateSection(UUID siteId, UUID pageId, UUID sectionId, UpdatePageSectionCommand command);
    void deleteSection(UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion);
}
