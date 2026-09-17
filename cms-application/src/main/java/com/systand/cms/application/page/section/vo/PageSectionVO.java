package com.systand.cms.application.page.section.vo;

import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageSectionVO {
	private UUID id;
	private UUID siteId;
	private UUID pageId;
	private String sectionKey;
	private String sectionType;
	private String adminLabel;
	private String anchorId;
	private Integer sortOrder;
	private Boolean isVisible;
	private Map<String, Object> settings;
	private Integer schemaVersion;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private UUID createdBy;
	private String createdByType;
	private UUID updatedBy;
	private String updatedByType;

	public static PageSectionVO from(PageSectionDO source) {
		PageSectionVO result = new PageSectionVO();
		result.setId(source.getId());
		result.setSiteId(source.getSiteId());
		result.setPageId(source.getPageId());
		result.setSectionKey(source.getSectionKey());
		result.setSectionType(source.getSectionType());
		result.setAdminLabel(source.getAdminLabel());
		result.setAnchorId(source.getAnchorId());
		result.setSortOrder(source.getSortOrder());
		result.setIsVisible(source.getIsVisible());
		result.setSettings(source.getSettings());
		result.setSchemaVersion(source.getSchemaVersion());
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
