package com.systand.cms.application.page.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.application.page.dto.CreatePageRequest;
import com.systand.cms.application.page.dto.PublishPageRequest;
import com.systand.cms.application.page.dto.SchedulePageRequest;
import com.systand.cms.application.page.dto.UpdatePageRequest;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import com.systand.cms.application.page.section.service.PageSectionTypeService;
import com.systand.cms.application.page.vo.PageVO;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
	private static final String PAGE_RESOURCE = "PAGE";
	private static final String PAGE_SECTION_RESOURCE = "PAGE_SECTION";

	private final PageMapper pageMapper;
	private final PageLocaleMapper pageLocaleMapper;
	private final PageSectionMapper pageSectionMapper;
	private final PageSectionLocaleMapper pageSectionLocaleMapper;
	private final SiteMapper siteMapper;
	private final CmsActorProvider actorProvider;
	private final CmsMediaPort mediaPort;
	private final CmsTenantProvider tenantProvider;
	private final PageSectionTypeService pageSectionTypeService;

	@Override
	public List<PageVO> getPages(UUID siteId, PagePublicationStatus status, PageKind pageKind, String keyword) {
		validateSite(siteId);
		LambdaQueryWrapper<PageDO> query = new LambdaQueryWrapper<PageDO>()
				.eq(PageDO::getSiteId, siteId)
				.eq(status != null, PageDO::getPublicationStatus, status)
				.eq(pageKind != null, PageDO::getPageKind, pageKind)
				.and(StringUtils.hasText(keyword), wrapper -> wrapper
						.like(PageDO::getCode, keyword.trim())
						.or()
						.like(PageDO::getRoutePath, keyword.trim()))
				.orderByDesc(PageDO::getIsHome)
				.orderByAsc(PageDO::getSortOrder)
				.orderByDesc(PageDO::getUpdatedAt);
		return pageMapper.selectList(query).stream().map(PageVO::from).toList();
	}

	@Override
	public PageVO getPage(UUID siteId, UUID pageId) {
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO createPage(UUID siteId, CreatePageRequest request) {
		validateSite(siteId);
		validateHomeRoute(request.getIsHome(), request.getRoutePath());
		CmsActor currentUser = actorProvider.requireActor();
		UUID userId = currentUser.userId();
		String actorType = actorType(currentUser);
		PageDO page = new PageDO();
		page.setTenantId(tenantProvider.requireTenantId());
		page.setSiteId(siteId);
		page.setCode(request.getCode());
		page.setRoutePath(request.getRoutePath());
		page.setPageKind(request.getPageKind() == null ? PageKind.STANDARD : request.getPageKind());
		page.setTemplateCode(request.getTemplateCode());
		page.setPublicationStatus(PagePublicationStatus.DRAFT);
		page.setIsHome(Boolean.TRUE.equals(request.getIsHome()));
		page.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
		page.setLayoutSettings(request.getLayoutSettings() == null ? new HashMap<>() : request.getLayoutSettings());
		page.setLockVersion(1);
		page.setCreatedBy(userId);
		page.setCreatedByType(actorType);
		page.setUpdatedBy(userId);
		page.setUpdatedByType(actorType);
		try {
			pageMapper.insert(page);
		} catch (DuplicateKeyException exception) {
			throw pageConflict();
		}
		return PageVO.from(requirePage(siteId, page.getId()));
	}

	@Override
	@Transactional
	public PageVO updatePage(UUID siteId, UUID pageId, UpdatePageRequest request) {
		PageDO current = requireLockVersion(siteId, pageId, request.getLockVersion());
		if (current.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
			throw badRequest("已归档页面不能直接编辑");
		}
		validateHomeRoute(request.getIsHome(), request.getRoutePath());
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, request.getLockVersion())
				.set(PageDO::getCode, request.getCode())
				.set(PageDO::getRoutePath, request.getRoutePath())
				.set(PageDO::getPageKind, request.getPageKind())
				.set(PageDO::getTemplateCode, request.getTemplateCode())
				.set(PageDO::getIsHome, request.getIsHome())
				.set(PageDO::getSortOrder, request.getSortOrder())
				.set(PageDO::getLayoutSettings, request.getLayoutSettings());
		try {
			ensureUpdated(pageMapper.update(null, update));
		} catch (DuplicateKeyException exception) {
			throw pageConflict();
		}
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public void deletePage(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		if (current.getPublicationStatus() == PagePublicationStatus.PUBLISHED) {
			throw badRequest("已发布页面必须先下线或归档后才能删除");
		}
		OffsetDateTime deletedAt = OffsetDateTime.now();
		List<PageSectionDO> sections = pageSectionMapper.selectList(
				new LambdaQueryWrapper<PageSectionDO>()
						.eq(PageSectionDO::getSiteId, siteId)
						.eq(PageSectionDO::getPageId, pageId));
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getDeletedAt, deletedAt);
		ensureUpdated(pageMapper.update(null, update));

		CmsActor currentUser = actorProvider.requireActor();
		pageLocaleMapper.update(null, new LambdaUpdateWrapper<PageLocaleDO>()
				.eq(PageLocaleDO::getSiteId, siteId)
				.eq(PageLocaleDO::getPageId, pageId)
				.set(PageLocaleDO::getDeletedAt, deletedAt)
				.set(PageLocaleDO::getUpdatedAt, deletedAt)
				.set(PageLocaleDO::getUpdatedBy, currentUser.userId())
				.set(PageLocaleDO::getUpdatedByType, actorType(currentUser))
				.setSql("lock_version = lock_version + 1"));

		// PostgreSQL ON DELETE CASCADE only applies to hard deletes. Keep the
		// complete page aggregate consistent when the CMS performs a soft delete.
		pageSectionLocaleMapper.update(null, new LambdaUpdateWrapper<PageSectionLocaleDO>()
				.eq(PageSectionLocaleDO::getSiteId, siteId)
				.eq(PageSectionLocaleDO::getPageId, pageId)
				.set(PageSectionLocaleDO::getDeletedAt, deletedAt)
				.set(PageSectionLocaleDO::getUpdatedAt, deletedAt)
				.set(PageSectionLocaleDO::getUpdatedBy, currentUser.userId())
				.set(PageSectionLocaleDO::getUpdatedByType, actorType(currentUser))
				.setSql("lock_version = lock_version + 1"));
		pageSectionMapper.update(null, new LambdaUpdateWrapper<PageSectionDO>()
				.eq(PageSectionDO::getSiteId, siteId)
				.eq(PageSectionDO::getPageId, pageId)
				.set(PageSectionDO::getDeletedAt, deletedAt)
				.set(PageSectionDO::getUpdatedAt, deletedAt)
				.set(PageSectionDO::getUpdatedBy, currentUser.userId())
					.set(PageSectionDO::getUpdatedByType, actorType(currentUser))
					.setSql("lock_version = lock_version + 1"));

		UUID tenantId = tenantProvider.requireTenantId();
		mediaPort.deleteResourceBindings(tenantId, PAGE_RESOURCE, pageId);
		sections.forEach(section -> mediaPort.deleteResourceBindings(
				tenantId, PAGE_SECTION_RESOURCE, section.getId()));
	}

	@Override
	@Transactional
	public PageVO submitForReview(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		requireStatus(current, PagePublicationStatus.DRAFT, PagePublicationStatus.UNPUBLISHED);
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getPublicationStatus, PagePublicationStatus.IN_REVIEW)
				.set(PageDO::getScheduledPublishAt, null)
				.set(PageDO::getScheduledUnpublishAt, null);
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO withdrawReview(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		requireStatus(current, PagePublicationStatus.IN_REVIEW);
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getPublicationStatus, draftStatus(current));
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO schedulePage(UUID siteId, UUID pageId, SchedulePageRequest request) {
		PageDO current = requireLockVersion(siteId, pageId, request.getLockVersion());
		requireStatus(current,
				PagePublicationStatus.DRAFT,
				PagePublicationStatus.IN_REVIEW,
				PagePublicationStatus.UNPUBLISHED);
		validateScheduleWindow(request.getScheduledPublishAt(), request.getScheduledUnpublishAt());
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, request.getLockVersion())
				.set(PageDO::getPublicationStatus, PagePublicationStatus.SCHEDULED)
				.set(PageDO::getScheduledPublishAt, request.getScheduledPublishAt())
				.set(PageDO::getScheduledUnpublishAt, request.getScheduledUnpublishAt());
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO cancelSchedule(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		requireStatus(current, PagePublicationStatus.SCHEDULED);
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getPublicationStatus, draftStatus(current))
				.set(PageDO::getScheduledPublishAt, null)
				.set(PageDO::getScheduledUnpublishAt, null);
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO publishPage(UUID siteId, UUID pageId, PublishPageRequest request) {
		PageDO current = requireLockVersion(siteId, pageId, request.getLockVersion());
		requireStatus(current,
				PagePublicationStatus.DRAFT,
				PagePublicationStatus.IN_REVIEW,
				PagePublicationStatus.SCHEDULED,
				PagePublicationStatus.UNPUBLISHED);
		validateSectionMediaForPublication(siteId, pageId);
		OffsetDateTime publishedAt = OffsetDateTime.now();
		OffsetDateTime scheduledUnpublishAt = request.getScheduledUnpublishAt();
		if (scheduledUnpublishAt == null
				&& current.getPublicationStatus() == PagePublicationStatus.SCHEDULED) {
			scheduledUnpublishAt = current.getScheduledUnpublishAt();
		}
		if (scheduledUnpublishAt != null && !scheduledUnpublishAt.isAfter(publishedAt)) {
			throw badRequest("scheduledUnpublishAt 必须晚于实际发布时间");
		}
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, request.getLockVersion())
				.set(PageDO::getPublicationStatus, PagePublicationStatus.PUBLISHED)
				.set(PageDO::getPublishedAt, publishedAt)
				.set(PageDO::getUnpublishedAt, null)
				.set(PageDO::getScheduledUnpublishAt, scheduledUnpublishAt);
		if (current.getFirstPublishedAt() == null) {
			update.set(PageDO::getFirstPublishedAt, publishedAt);
		}
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO unpublishPage(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		requireStatus(current, PagePublicationStatus.PUBLISHED);
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getPublicationStatus, PagePublicationStatus.UNPUBLISHED)
				.set(PageDO::getUnpublishedAt, OffsetDateTime.now());
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	@Override
	@Transactional
	public PageVO archivePage(UUID siteId, UUID pageId, Integer lockVersion) {
		PageDO current = requireLockVersion(siteId, pageId, lockVersion);
		if (current.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
			throw badRequest("页面已经归档");
		}
		OffsetDateTime now = OffsetDateTime.now();
		LambdaUpdateWrapper<PageDO> update = versionedUpdate(siteId, pageId, lockVersion)
				.set(PageDO::getPublicationStatus, PagePublicationStatus.ARCHIVED)
				.set(PageDO::getArchivedAt, now);
		if (current.getPublicationStatus() == PagePublicationStatus.PUBLISHED) {
			update.set(PageDO::getUnpublishedAt, now);
		}
		ensureUpdated(pageMapper.update(null, update));
		return PageVO.from(requirePage(siteId, pageId));
	}

	private void validateSite(UUID siteId) {
		boolean exists = siteMapper.exists(new LambdaQueryWrapper<SiteDO>()
				.eq(SiteDO::getId, siteId)
				.eq(SiteDO::getTenantId, tenantProvider.requireTenantId())
				.eq(SiteDO::getStatus, "ACTIVE"));
		if (!exists) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用");
		}
	}

	private PageDO requirePage(UUID siteId, UUID pageId) {
		PageDO page = pageMapper.selectOne(new LambdaQueryWrapper<PageDO>()
				.eq(PageDO::getId, pageId)
				.eq(PageDO::getTenantId, tenantProvider.requireTenantId())
				.eq(PageDO::getSiteId, siteId));
		if (page == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在");
		}
		return page;
	}

	private PageDO requireLockVersion(UUID siteId, UUID pageId, Integer lockVersion) {
		if (lockVersion == null) {
			throw badRequest("lockVersion 不能为空");
		}
		PageDO page = requirePage(siteId, pageId);
		if (!lockVersion.equals(page.getLockVersion())) {
			throw new CmsException(CmsErrorCode.CONFLICT, "页面已被其他操作更新，请刷新后重试");
		}
		return page;
	}

	private LambdaUpdateWrapper<PageDO> versionedUpdate(UUID siteId, UUID pageId, Integer lockVersion) {
		CmsActor currentUser = actorProvider.requireActor();
		return new LambdaUpdateWrapper<PageDO>()
				.eq(PageDO::getId, pageId)
				.eq(PageDO::getSiteId, siteId)
				.eq(PageDO::getLockVersion, lockVersion)
				.set(PageDO::getUpdatedBy, currentUser.userId())
				.set(PageDO::getUpdatedByType, actorType(currentUser))
				.set(PageDO::getUpdatedAt, OffsetDateTime.now())
				.set(PageDO::getLockVersion, lockVersion + 1);
	}

	private void ensureUpdated(int affectedRows) {
		if (affectedRows != 1) {
			throw new CmsException(CmsErrorCode.CONFLICT, "页面已被其他操作更新，请刷新后重试");
		}
	}

	private void validateScheduleWindow(OffsetDateTime scheduledPublishAt, OffsetDateTime scheduledUnpublishAt) {
		if (scheduledUnpublishAt != null && !scheduledUnpublishAt.isAfter(scheduledPublishAt)) {
			throw badRequest("scheduledUnpublishAt 必须晚于 scheduledPublishAt");
		}
	}

	private void validateSectionMediaForPublication(UUID siteId, UUID pageId) {
		List<PageSectionDO> sections = pageSectionMapper.selectList(
				new LambdaQueryWrapper<PageSectionDO>()
						.eq(PageSectionDO::getSiteId, siteId)
						.eq(PageSectionDO::getPageId, pageId)
						.eq(PageSectionDO::getIsVisible, true));
		for (PageSectionDO section : sections) {
			PageSectionTypeDefinition sectionType = pageSectionTypeService.requireAvailableDefinition(
					section.getSectionType(), section.getSchemaVersion());
			if (sectionType.requiredAnyOfMediaRoles().isEmpty()) {
				continue;
			}
			var assets = mediaPort.findBindings(
					tenantProvider.requireTenantId(), PAGE_SECTION_RESOURCE, section.getId(), null);
			boolean hasRequiredMedia = assets.stream()
					.anyMatch(asset -> sectionType.requiredAnyOfMediaRoles().contains(asset.role()));
			if (!hasRequiredMedia) {
				throw badRequest("区块 " + section.getSectionKey() + " 至少需要一个媒体角色: "
						+ String.join(" 或 ", sectionType.requiredAnyOfMediaRoles()));
			}
		}
	}

	private void validateHomeRoute(Boolean isHome, String routePath) {
		if (Boolean.TRUE.equals(isHome) != "/".equals(routePath)) {
			throw badRequest("首页必须使用 / 路由，/ 路由也必须设置 isHome=true");
		}
	}

	private void requireStatus(PageDO page, PagePublicationStatus... allowedStatuses) {
		for (PagePublicationStatus allowedStatus : allowedStatuses) {
			if (page.getPublicationStatus() == allowedStatus) {
				return;
			}
		}
		throw badRequest("当前页面状态不允许执行此操作: " + page.getPublicationStatus().toValue());
	}

	private PagePublicationStatus draftStatus(PageDO page) {
		return page.getPublishedAt() == null
				? PagePublicationStatus.DRAFT
				: PagePublicationStatus.UNPUBLISHED;
	}

	private String actorType(CmsActor user) {
		return user.auditType();
	}

	private CmsException pageConflict() {
		return new CmsException(CmsErrorCode.CONFLICT, "同一站点下的页面编码、路由或首页设置重复");
	}

	private CmsException badRequest(String message) {
		return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
	}
}
