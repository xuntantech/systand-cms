package com.systand.cms.application.page.service;

import com.systand.cms.application.page.dto.CreatePageLocaleRequest;
import com.systand.cms.application.page.dto.UpdatePageLocaleRequest;
import com.systand.cms.application.page.vo.PageLocaleVO;

import java.util.List;
import java.util.UUID;

public interface PageLocaleService {
	List<PageLocaleVO> getPageLocales(UUID siteId, UUID pageId);

	PageLocaleVO getPageLocale(UUID siteId, UUID pageId, UUID localeId);

	PageLocaleVO createPageLocale(UUID siteId, UUID pageId, CreatePageLocaleRequest request);

	PageLocaleVO updatePageLocale(UUID siteId, UUID pageId, UUID localeId, UpdatePageLocaleRequest request);

	void deletePageLocale(UUID siteId, UUID pageId, UUID localeId, Integer lockVersion);
}
