package com.systand.cms.application.page.section.service;

import com.systand.cms.application.page.section.dto.CreatePageSectionRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionRequest;
import com.systand.cms.application.page.section.vo.PageSectionVO;

import java.util.List;
import java.util.UUID;

public interface PageSectionService {
	List<PageSectionVO> getSections(UUID siteId, UUID pageId, Boolean visible, String sectionType);

	PageSectionVO getSection(UUID siteId, UUID pageId, UUID sectionId);

	PageSectionVO createSection(UUID siteId, UUID pageId, CreatePageSectionRequest request);

	PageSectionVO updateSection(
			UUID siteId, UUID pageId, UUID sectionId, UpdatePageSectionRequest request);

	void deleteSection(UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion);
}
