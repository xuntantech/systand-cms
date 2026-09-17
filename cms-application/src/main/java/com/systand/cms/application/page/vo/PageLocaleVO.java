package com.systand.cms.application.page.vo;

import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageLocaleVO {
	private UUID id;
	private UUID siteId;
	private UUID pageId;
	private String locale;
	private String title;
	private String summary;
	private String seoTitle;
	private String seoDescription;
	private Map<String, Object> metadata;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private UUID createdBy;
	private String createdByType;
	private UUID updatedBy;
	private String updatedByType;

	public static PageLocaleVO from(PageLocaleDO source) {
		PageLocaleVO result = new PageLocaleVO();
		result.setId(source.getId());
		result.setSiteId(source.getSiteId());
		result.setPageId(source.getPageId());
		result.setLocale(source.getLocale());
		result.setTitle(source.getTitle());
		result.setSummary(source.getSummary());
		result.setSeoTitle(source.getSeoTitle());
		result.setSeoDescription(source.getSeoDescription());
		result.setMetadata(source.getMetadata());
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
