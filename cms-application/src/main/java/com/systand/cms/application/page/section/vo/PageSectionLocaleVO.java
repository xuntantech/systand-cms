package com.systand.cms.application.page.section.vo;

import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.api.page.SectionTranslationStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageSectionLocaleVO {
	private UUID id;
	private UUID siteId;
	private UUID pageId;
	private UUID sectionId;
	private String locale;
	private SectionTranslationStatus translationStatus;
	private Map<String, Object> content;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private UUID createdBy;
	private String createdByType;
	private UUID updatedBy;
	private String updatedByType;

	public static PageSectionLocaleVO from(PageSectionLocaleDO source) {
		PageSectionLocaleVO result = new PageSectionLocaleVO();
		result.setId(source.getId());
		result.setSiteId(source.getSiteId());
		result.setPageId(source.getPageId());
		result.setSectionId(source.getSectionId());
		result.setLocale(source.getLocale());
		result.setTranslationStatus(source.getTranslationStatus());
		result.setContent(source.getContent());
		result.setLockVersion(source.getLockVersion());
		result.setCreatedAt(source.getCreatedAt());
		result.setUpdatedAt(source.getUpdatedAt());
		result.setCreatedBy(source.getCreatedBy());
		result.setCreatedByType(source.getCreatedByType());
		result.setUpdatedBy(source.getUpdatedBy());
		result.setUpdatedByType(source.getUpdatedByType());
		return result;
	}
}
