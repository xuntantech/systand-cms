package com.systand.cms.application.page.section.service;

import com.systand.cms.application.page.section.dto.CreatePageSectionLocaleRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionLocaleRequest;
import com.systand.cms.application.page.section.vo.PageSectionLocaleVO;

import java.util.List;
import java.util.UUID;

public interface PageSectionLocaleService {
	List<PageSectionLocaleVO> getSectionLocales(UUID siteId, UUID pageId, UUID sectionId);

	PageSectionLocaleVO getSectionLocale(
			UUID siteId, UUID pageId, UUID sectionId, UUID localeId);

	PageSectionLocaleVO createSectionLocale(
			UUID siteId, UUID pageId, UUID sectionId, CreatePageSectionLocaleRequest request);

	PageSectionLocaleVO updateSectionLocale(
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			UUID localeId,
			UpdatePageSectionLocaleRequest request);

	void deleteSectionLocale(
			UUID siteId, UUID pageId, UUID sectionId, UUID localeId, Integer lockVersion);
}
