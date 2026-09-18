package com.systand.cms.application.page.section.service;

import com.systand.cms.application.page.section.vo.PageSectionTypeVO;
import com.systand.cms.core.section.PageSectionTypeDefinition;

import java.util.List;
import java.util.UUID;

public interface PageSectionTypeService {
	List<PageSectionTypeVO> getAvailableSectionTypes();

	PageSectionTypeDefinition requireAvailableDefinition(String code, Integer schemaVersion);

	void initializeBuiltInsForTenant(UUID tenantId);

}
