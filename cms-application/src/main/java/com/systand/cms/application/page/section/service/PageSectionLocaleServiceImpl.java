package com.systand.cms.application.page.section.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.application.page.section.dto.CreatePageSectionLocaleRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionLocaleRequest;
import com.systand.cms.api.page.SectionTranslationStatus;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.application.page.section.vo.PageSectionLocaleVO;
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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageSectionLocaleServiceImpl implements PageSectionLocaleService {
	private final PageSectionLocaleMapper pageSectionLocaleMapper;
	private final PageSectionMapper pageSectionMapper;
	private final PageMapper pageMapper;
	private final SiteMapper siteMapper;
	private final CmsActorProvider actorProvider;
	private final CmsTenantProvider tenantProvider;

	@Override
	public List<PageSectionLocaleVO> getSectionLocales(
			UUID siteId, UUID pageId, UUID sectionId) {
		requireContext(siteId, pageId, sectionId, false);
		return pageSectionLocaleMapper.selectList(new LambdaQueryWrapper<PageSectionLocaleDO>()
				.eq(PageSectionLocaleDO::getSiteId, siteId)
				.eq(PageSectionLocaleDO::getPageId, pageId)
				.eq(PageSectionLocaleDO::getSectionId, sectionId)
				.orderByAsc(PageSectionLocaleDO::getLocale))
				.stream()
				.map(PageSectionLocaleVO::from)
				.toList();
	}

	@Override
	public PageSectionLocaleVO getSectionLocale(
			UUID siteId, UUID pageId, UUID sectionId, UUID localeId) {
		requireContext(siteId, pageId, sectionId, false);
		return PageSectionLocaleVO.from(requireLocale(siteId, pageId, sectionId, localeId));
	}

	@Override
	@Transactional
	public PageSectionLocaleVO createSectionLocale(
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			CreatePageSectionLocaleRequest request) {
		SectionContext context = requireContext(siteId, pageId, sectionId, true);
		String locale = requireEnabledLocale(context.site(), request.getLocale());
		validateContentState(request.getTranslationStatus(), request.getContent());
		CmsActor currentUser = actorProvider.requireActor();
		String actorType = actorType(currentUser);

		PageSectionLocaleDO sectionLocale = new PageSectionLocaleDO();
		sectionLocale.setTenantId(context.page().getTenantId());
		sectionLocale.setSiteId(siteId);
		sectionLocale.setPageId(pageId);
		sectionLocale.setSectionId(sectionId);
		sectionLocale.setLocale(locale);
		sectionLocale.setTranslationStatus(request.getTranslationStatus());
		sectionLocale.setContent(new HashMap<>(request.getContent()));
		sectionLocale.setLockVersion(1);
		sectionLocale.setCreatedBy(currentUser.userId());
		sectionLocale.setCreatedByType(actorType);
		sectionLocale.setUpdatedBy(currentUser.userId());
		sectionLocale.setUpdatedByType(actorType);
		try {
			pageSectionLocaleMapper.insert(sectionLocale);
		} catch (DuplicateKeyException exception) {
			throw localeConflict(locale);
		}
		return PageSectionLocaleVO.from(
				requireLocale(siteId, pageId, sectionId, sectionLocale.getId()));
	}

	@Override
	@Transactional
	public PageSectionLocaleVO updateSectionLocale(
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			UUID localeId,
			UpdatePageSectionLocaleRequest request) {
		requireContext(siteId, pageId, sectionId, true);
		requireLockVersion(siteId, pageId, sectionId, localeId, request.getLockVersion());
		validateContentState(request.getTranslationStatus(), request.getContent());
		CmsActor currentUser = actorProvider.requireActor();

		LambdaUpdateWrapper<PageSectionLocaleDO> update = versionedUpdate(
				siteId, pageId, sectionId, localeId, request.getLockVersion(), currentUser)
				.set(PageSectionLocaleDO::getTranslationStatus, request.getTranslationStatus())
				.set(PageSectionLocaleDO::getContent, new HashMap<>(request.getContent()),
						"jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler");
		ensureUpdated(pageSectionLocaleMapper.update(null, update));
		return PageSectionLocaleVO.from(requireLocale(siteId, pageId, sectionId, localeId));
	}

	@Override
	@Transactional
	public void deleteSectionLocale(
			UUID siteId, UUID pageId, UUID sectionId, UUID localeId, Integer lockVersion) {
		requireContext(siteId, pageId, sectionId, true);
		requireLockVersion(siteId, pageId, sectionId, localeId, lockVersion);
		CmsActor currentUser = actorProvider.requireActor();
		ensureUpdated(pageSectionLocaleMapper.update(null,
				versionedUpdate(siteId, pageId, sectionId, localeId, lockVersion, currentUser)
						.set(PageSectionLocaleDO::getDeletedAt, OffsetDateTime.now())));
	}

	private SectionContext requireContext(
			UUID siteId, UUID pageId, UUID sectionId, boolean editable) {
		SiteDO site = siteMapper.selectOne(new LambdaQueryWrapper<SiteDO>()
				.eq(SiteDO::getId, siteId)
				.eq(SiteDO::getTenantId, tenantProvider.requireTenantId())
				.eq(SiteDO::getStatus, "ACTIVE"));
		if (site == null) {
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
			throw badRequest("已归档页面不能编辑区块语言内容");
		}
		PageSectionDO section = pageSectionMapper.selectOne(new LambdaQueryWrapper<PageSectionDO>()
				.eq(PageSectionDO::getId, sectionId)
				.eq(PageSectionDO::getSiteId, siteId)
				.eq(PageSectionDO::getPageId, pageId));
		if (section == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面区块不存在");
		}
		return new SectionContext(site, page, section);
	}

	private PageSectionLocaleDO requireLocale(
			UUID siteId, UUID pageId, UUID sectionId, UUID localeId) {
		PageSectionLocaleDO sectionLocale = pageSectionLocaleMapper.selectOne(
				new LambdaQueryWrapper<PageSectionLocaleDO>()
						.eq(PageSectionLocaleDO::getId, localeId)
						.eq(PageSectionLocaleDO::getSiteId, siteId)
						.eq(PageSectionLocaleDO::getPageId, pageId)
						.eq(PageSectionLocaleDO::getSectionId, sectionId));
		if (sectionLocale == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面区块语言内容不存在");
		}
		return sectionLocale;
	}

	private PageSectionLocaleDO requireLockVersion(
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			UUID localeId,
			Integer lockVersion) {
		if (lockVersion == null) {
			throw badRequest("lockVersion 不能为空");
		}
		PageSectionLocaleDO current = requireLocale(siteId, pageId, sectionId, localeId);
		if (!lockVersion.equals(current.getLockVersion())) {
			throw conflict("页面区块语言内容已被其他操作更新，请刷新后重试");
		}
		return current;
	}

	private LambdaUpdateWrapper<PageSectionLocaleDO> versionedUpdate(
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			UUID localeId,
			Integer lockVersion,
			CmsActor currentUser) {
		return new LambdaUpdateWrapper<PageSectionLocaleDO>()
				.eq(PageSectionLocaleDO::getId, localeId)
				.eq(PageSectionLocaleDO::getSiteId, siteId)
				.eq(PageSectionLocaleDO::getPageId, pageId)
				.eq(PageSectionLocaleDO::getSectionId, sectionId)
				.eq(PageSectionLocaleDO::getLockVersion, lockVersion)
				.set(PageSectionLocaleDO::getUpdatedBy, currentUser.userId())
				.set(PageSectionLocaleDO::getUpdatedByType, actorType(currentUser))
				.set(PageSectionLocaleDO::getUpdatedAt, OffsetDateTime.now())
				.set(PageSectionLocaleDO::getLockVersion, lockVersion + 1);
	}

	private String requireEnabledLocale(SiteDO site, String requestedLocale) {
		String locale = requestedLocale.trim();
		if (site.getEnabledLocales() != null && !site.getEnabledLocales().isEmpty()) {
			return site.getEnabledLocales().stream()
					.filter(candidate -> candidate.equalsIgnoreCase(locale))
					.findFirst()
					.orElseThrow(() -> badRequest("站点未启用语言: " + locale));
		}
		if (StringUtils.hasText(site.getDefaultLocale())) {
			if (site.getDefaultLocale().equalsIgnoreCase(locale)) {
				return site.getDefaultLocale();
			}
			throw badRequest("站点未启用语言: " + locale);
		}
		return locale;
	}

	/**
	 * Enforces the cross-field rule shared by every component type. Detailed
	 * sectionType/schemaVersion validation belongs in a component schema registry
	 * once those contracts are introduced; keeping that extension point here
	 * prevents controllers and persistence code from knowing component internals.
	 */
	private void validateContentState(
			SectionTranslationStatus status, Map<String, Object> content) {
		if (status == null || content == null) {
			throw badRequest("translationStatus 和 content 不能为空");
		}
		if (status == SectionTranslationStatus.COMPLETE && content.isEmpty()) {
			throw badRequest("标记为 COMPLETE 的区块语言内容不能为空");
		}
	}

	private void ensureUpdated(int affectedRows) {
		if (affectedRows != 1) {
			throw conflict("页面区块语言内容已被其他操作更新，请刷新后重试");
		}
	}

	private String actorType(CmsActor user) {
		return user.auditType();
	}

	private CmsException localeConflict(String locale) {
		return conflict("该页面区块已存在 " + locale + " 语言内容");
	}

	private CmsException conflict(String message) {
		return new CmsException(CmsErrorCode.CONFLICT, message);
	}

	private CmsException badRequest(String message) {
		return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
	}

	private record SectionContext(SiteDO site, PageDO page, PageSectionDO section) {
	}
}
