package com.systand.cms.api.page;

import java.util.List;
import java.util.UUID;

public interface CmsPageSectionLocaleService {
    List<PageSectionLocaleVO> getSectionLocales(UUID siteId, UUID pageId, UUID sectionId);
    PageSectionLocaleVO getSectionLocale(UUID siteId, UUID pageId, UUID sectionId, UUID localeId);
    PageSectionLocaleVO createSectionLocale(UUID siteId, UUID pageId, UUID sectionId, CreatePageSectionLocaleCommand command);
    PageSectionLocaleVO updateSectionLocale(UUID siteId, UUID pageId, UUID sectionId, UUID localeId, UpdatePageSectionLocaleCommand command);
    void deleteSectionLocale(UUID siteId, UUID pageId, UUID sectionId, UUID localeId, Integer lockVersion);
}
