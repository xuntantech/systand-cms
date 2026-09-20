package com.systand.cms.api.page;

import java.util.List;
import java.util.UUID;

public interface CmsPageLocaleService {
    List<PageLocaleVO> getPageLocales(UUID siteId, UUID pageId);
    PageLocaleVO getPageLocale(UUID siteId, UUID pageId, UUID localeId);
    PageLocaleVO createPageLocale(UUID siteId, UUID pageId, CreatePageLocaleCommand command);
    PageLocaleVO updatePageLocale(UUID siteId, UUID pageId, UUID localeId, UpdatePageLocaleCommand command);
    void deletePageLocale(UUID siteId, UUID pageId, UUID localeId, Integer lockVersion);
}
