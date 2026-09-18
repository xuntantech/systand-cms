package com.systand.cms.application.page.section.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.core.section.PageSectionTypeDefinition;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.application.page.section.dto.CreatePageSectionRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionRequest;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.application.page.section.vo.PageSectionVO;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageSectionServiceImpl implements PageSectionService {
	private static final String PAGE_SECTION_RESOURCE = "PAGE_SECTION";

	private final PageSectionMapper pageSectionMapper;
	private final PageSectionLocaleMapper pageSectionLocaleMapper;
	private final PageMapper pageMapper;
	private final SiteMapper siteMapper;
	private final CmsActorProvider actorProvider;
	private final CmsMediaPort mediaPort;
	private final CmsTenantProvider tenantProvider;
	private final PageSectionTypeService pageSectionTypeService;

	@Override
	public List<PageSectionVO> getSections(
			UUID siteId, UUID pageId, Boolean visible, String sectionType) {
		requirePageContext(siteId, pageId, false);
		LambdaQueryWrapper<PageSectionDO> query = new LambdaQueryWrapper<PageSectionDO>()
				.eq(PageSectionDO::getSiteId, siteId)
				.eq(PageSectionDO::getPageId, pageId)
				.eq(visible != null, PageSectionDO::getIsVisible, visible)
				.eq(StringUtils.hasText(sectionType), PageSectionDO::getSectionType,
						StringUtils.hasText(sectionType) ? sectionType.trim() : null)
				.orderByAsc(PageSectionDO::getSortOrder)
				.orderByAsc(PageSectionDO::getId);
		return pageSectionMapper.selectList(query).stream().map(PageSectionVO::from).toList();
	}

	@Override
	public PageSectionVO getSection(UUID siteId, UUID pageId, UUID sectionId) {
		requirePageContext(siteId, pageId, false);
		return PageSectionVO.from(requireSection(siteId, pageId, sectionId));
	}

	@Override
	@Transactional
	public PageSectionVO createSection(
			UUID siteId, UUID pageId, CreatePageSectionRequest request) {
		PageDO page = requirePageContext(siteId, pageId, true);
		int schemaVersion = request.getSchemaVersion() == null ? 1 : request.getSchemaVersion();
		PageSectionTypeDefinition sectionType = pageSectionTypeService.requireAvailableDefinition(
				request.getSectionType(), schemaVersion);
		CmsActor currentUser = actorProvider.requireActor();
		String actorType = actorType(currentUser);

		PageSectionDO section = new PageSectionDO();
		section.setTenantId(page.getTenantId());
		section.setSiteId(siteId);
		section.setPageId(pageId);
		section.setSectionKey(request.getSectionKey().trim());
		section.setSectionType(sectionType.code());
		section.setAdminLabel(trimToNull(request.getAdminLabel()));
		section.setAnchorId(trimToNull(request.getAnchorId()));
		section.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
		section.setIsVisible(request.getIsVisible() == null || request.getIsVisible());
		section.setSettings(sectionType.mergeSettings(request.getSettings()));
		section.setSchemaVersion(schemaVersion);
		section.setLockVersion(1);
		section.setCreatedBy(currentUser.userId());
		section.setCreatedByType(actorType);
		section.setUpdatedBy(currentUser.userId());
		section.setUpdatedByType(actorType);
		try {
			pageSectionMapper.insert(section);
		} catch (DuplicateKeyException exception) {
			throw sectionConflict();
		}
		return PageSectionVO.from(requireSection(siteId, pageId, section.getId()));
	}

	@Override
	@Transactional
	public PageSectionVO updateSection(
			UUID siteId, UUID pageId, UUID sectionId, UpdatePageSectionRequest request) {
		requirePageContext(siteId, pageId, true);
		PageSectionDO current = requireLockVersion(
				siteId, pageId, sectionId, request.getLockVersion());
		PageSectionTypeDefinition sectionType = pageSectionTypeService.requireAvailableDefinition(
				request.getSectionType(), request.getSchemaVersion());
		if (!current.getSectionType().equals(sectionType.code())) {
			throw badRequest("sectionType 创建后不能修改");
		}
		if (!current.getSectionKey().equals(request.getSectionKey().trim())) {
			throw badRequest("sectionKey 创建后不能修改");
		}

		LambdaUpdateWrapper<PageSectionDO> update = versionedUpdate(
				siteId, pageId, sectionId, request.getLockVersion())
				.set(PageSectionDO::getSectionKey, request.getSectionKey().trim())
				.set(PageSectionDO::getSectionType, sectionType.code())
				.set(PageSectionDO::getAdminLabel, trimToNull(request.getAdminLabel()))
				.set(PageSectionDO::getAnchorId, trimToNull(request.getAnchorId()))
				.set(PageSectionDO::getSortOrder, request.getSortOrder())
				.set(PageSectionDO::getIsVisible, request.getIsVisible())
				.set(PageSectionDO::getSettings, sectionType.mergeSettings(request.getSettings()),
						"jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler")
				.set(PageSectionDO::getSchemaVersion, request.getSchemaVersion());
		try {
			ensureUpdated(pageSectionMapper.update(null, update));
		} catch (DuplicateKeyException exception) {
			throw sectionConflict();
		}
		return PageSectionVO.from(requireSection(siteId, pageId, sectionId));
	}

	@Override
	@Transactional
	public void deleteSection(UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion) {
		requirePageContext(siteId, pageId, true);
		requireLockVersion(siteId, pageId, sectionId, lockVersion);
		OffsetDateTime deletedAt = OffsetDateTime.now();
		CmsActor currentUser = actorProvider.requireActor();

		ensureUpdated(pageSectionMapper.update(null,
				versionedUpdate(siteId, pageId, sectionId, lockVersion)
						.set(PageSectionDO::getDeletedAt, deletedAt)));

		// The database cascades only hard deletes. A business soft delete must
		// explicitly hide every locale in the same transaction.
		pageSectionLocaleMapper.update(null, new LambdaUpdateWrapper<PageSectionLocaleDO>()
				.eq(PageSectionLocaleDO::getSiteId, siteId)
				.eq(PageSectionLocaleDO::getPageId, pageId)
				.eq(PageSectionLocaleDO::getSectionId, sectionId)
				.set(PageSectionLocaleDO::getDeletedAt, deletedAt)
				.set(PageSectionLocaleDO::getUpdatedAt, deletedAt)
				.set(PageSectionLocaleDO::getUpdatedBy, currentUser.userId())
				.set(PageSectionLocaleDO::getUpdatedByType, actorType(currentUser))
				.setSql("lock_version = lock_version + 1"));

		mediaPort.deleteResourceBindings(tenantProvider.requireTenantId(), PAGE_SECTION_RESOURCE, sectionId);
	}

	private PageDO requirePageContext(UUID siteId, UUID pageId, boolean editable) {
		boolean siteExists = siteMapper.exists(new LambdaQueryWrapper<SiteDO>()
				.eq(SiteDO::getId, siteId)
				.eq(SiteDO::getTenantId, tenantProvider.requireTenantId())
				.eq(SiteDO::getStatus, "ACTIVE"));
		if (!siteExists) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用");
		}
		PageDO page = pageMapper.selectOne(new LambdaQueryWrapper<PageDO>()
				.eq(PageDO::getId, pageId)
				.eq(PageDO::getTenantId, tenantProvider.requireTenantId())
				.eq(PageDO::getSiteId, siteId));
		if (page == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在");
		}
		if (editable && page.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
			throw badRequest("已归档页面不能编辑区块");
		}
		return page;
	}

	private PageSectionDO requireSection(UUID siteId, UUID pageId, UUID sectionId) {
		PageSectionDO section = pageSectionMapper.selectOne(new LambdaQueryWrapper<PageSectionDO>()
				.eq(PageSectionDO::getId, sectionId)
				.eq(PageSectionDO::getSiteId, siteId)
				.eq(PageSectionDO::getPageId, pageId));
		if (section == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面区块不存在");
		}
		return section;
	}

	private PageSectionDO requireLockVersion(
			UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion) {
		if (lockVersion == null) {
			throw badRequest("lockVersion 不能为空");
		}
		PageSectionDO section = requireSection(siteId, pageId, sectionId);
		if (!lockVersion.equals(section.getLockVersion())) {
			throw conflict("页面区块已被其他操作更新，请刷新后重试");
		}
		return section;
	}

	private LambdaUpdateWrapper<PageSectionDO> versionedUpdate(
			UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion) {
		CmsActor currentUser = actorProvider.requireActor();
		return new LambdaUpdateWrapper<PageSectionDO>()
				.eq(PageSectionDO::getId, sectionId)
				.eq(PageSectionDO::getSiteId, siteId)
				.eq(PageSectionDO::getPageId, pageId)
				.eq(PageSectionDO::getLockVersion, lockVersion)
				.set(PageSectionDO::getUpdatedBy, currentUser.userId())
				.set(PageSectionDO::getUpdatedByType, actorType(currentUser))
				.set(PageSectionDO::getUpdatedAt, OffsetDateTime.now())
				.set(PageSectionDO::getLockVersion, lockVersion + 1);
	}

	private void ensureUpdated(int affectedRows) {
		if (affectedRows != 1) {
			throw conflict("页面区块已被其他操作更新，请刷新后重试");
		}
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String actorType(CmsActor user) {
		return user.auditType();
	}

	private CmsException sectionConflict() {
		return conflict("同一页面下的区块编码或锚点重复");
	}

	private CmsException conflict(String message) {
		return new CmsException(CmsErrorCode.CONFLICT, message);
	}

	private CmsException badRequest(String message) {
		return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
	}
}
