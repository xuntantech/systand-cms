package com.systand.cms.application.page.vo;

import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class PageVO {
	private UUID id;
	private UUID siteId;
	private String code;
	private String routePath;
	private PageKind pageKind;
	private String templateCode;
	private PagePublicationStatus publicationStatus;
	private Boolean isHome;
	private Integer sortOrder;
	private Map<String, Object> layoutSettings;
	private OffsetDateTime scheduledPublishAt;
	private OffsetDateTime scheduledUnpublishAt;
	private OffsetDateTime publishedAt;
	private OffsetDateTime firstPublishedAt;
	private OffsetDateTime unpublishedAt;
	private OffsetDateTime archivedAt;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private UUID createdBy;
	private String createdByType;
	private UUID updatedBy;
	private String updatedByType;

	public static PageVO from(PageDO page) {
		PageVO result = new PageVO();
		result.setId(page.getId());
		result.setSiteId(page.getSiteId());
		result.setCode(page.getCode());
		result.setRoutePath(page.getRoutePath());
		result.setPageKind(page.getPageKind());
		result.setTemplateCode(page.getTemplateCode());
		result.setPublicationStatus(page.getPublicationStatus());
		result.setIsHome(page.getIsHome());
		result.setSortOrder(page.getSortOrder());
		result.setLayoutSettings(page.getLayoutSettings());
		result.setScheduledPublishAt(page.getScheduledPublishAt());
		result.setScheduledUnpublishAt(page.getScheduledUnpublishAt());
		result.setPublishedAt(page.getPublishedAt());
		result.setFirstPublishedAt(page.getFirstPublishedAt());
		result.setUnpublishedAt(page.getUnpublishedAt());
		result.setArchivedAt(page.getArchivedAt());
		result.setLockVersion(page.getLockVersion());
		result.setCreatedAt(page.getCreatedAt());
		result.setUpdatedAt(page.getUpdatedAt());
		result.setCreatedBy(page.getCreatedBy());
		result.setCreatedByType(page.getCreatedByType());
		result.setUpdatedBy(page.getUpdatedBy());
		result.setUpdatedByType(page.getUpdatedByType());
		return result;
	}
}
