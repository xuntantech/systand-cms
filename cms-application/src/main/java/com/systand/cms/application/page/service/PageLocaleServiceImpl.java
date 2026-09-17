package com.systand.cms.application.page.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import com.systand.cms.application.page.dto.CreatePageLocaleRequest;
import com.systand.cms.application.page.dto.UpdatePageLocaleRequest;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.application.page.vo.PageLocaleVO;
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
public class PageLocaleServiceImpl implements PageLocaleService {
	private final PageLocaleMapper pageLocaleMapper;
	private final PageMapper pageMapper;
	private final SiteMapper siteMapper;
	private final CmsActorProvider actorProvider;
	private final CmsTenantProvider tenantProvider;

	@Override
	public List<PageLocaleVO> getPageLocales(UUID siteId, UUID pageId) {
		requirePageContext(siteId, pageId);
		return pageLocaleMapper.selectList(new LambdaQueryWrapper<PageLocaleDO>()
				.eq(PageLocaleDO::getSiteId, siteId)
				.eq(PageLocaleDO::getPageId, pageId)
				.orderByAsc(PageLocaleDO::getLocale))
				.stream()
				.map(PageLocaleVO::from)
				.toList();
	}

	@Override
	public PageLocaleVO getPageLocale(UUID siteId, UUID pageId, UUID localeId) {
		requirePageContext(siteId, pageId);
		return PageLocaleVO.from(requirePageLocale(siteId, pageId, localeId));
	}

	@Override
	@Transactional
	public PageLocaleVO createPageLocale(UUID siteId, UUID pageId, CreatePageLocaleRequest request) {
		PageContext context = requireEditablePageContext(siteId, pageId);
		String locale = requireEnabledLocale(context.site(), request.getLocale());
		CmsActor currentUser = actorProvider.requireActor();
		String actorType = actorType(currentUser);

		PageLocaleDO pageLocale = new PageLocaleDO();
		pageLocale.setTenantId(context.page().getTenantId());
		pageLocale.setSiteId(siteId);
		pageLocale.setPageId(pageId);
		pageLocale.setLocale(locale);
		pageLocale.setTitle(request.getTitle().trim());
		pageLocale.setSummary(trimToNull(request.getSummary()));
		pageLocale.setSeoTitle(trimToNull(request.getSeoTitle()));
		pageLocale.setSeoDescription(trimToNull(request.getSeoDescription()));
		pageLocale.setMetadata(new HashMap<>(request.getMetadata()));
		pageLocale.setLockVersion(1);
		pageLocale.setCreatedBy(currentUser.userId());
		pageLocale.setCreatedByType(actorType);
		pageLocale.setUpdatedBy(currentUser.userId());
		pageLocale.setUpdatedByType(actorType);
		try {
			pageLocaleMapper.insert(pageLocale);
		} catch (DuplicateKeyException exception) {
			throw localeConflict(locale);
		}
		return PageLocaleVO.from(requirePageLocale(siteId, pageId, pageLocale.getId()));
	}

	@Override
	@Transactional
	public PageLocaleVO updatePageLocale(
			UUID siteId,
			UUID pageId,
			UUID localeId,
			UpdatePageLocaleRequest request) {
		requireEditablePageContext(siteId, pageId);
		requireLockVersion(siteId, pageId, localeId, request.getLockVersion());
		CmsActor currentUser = actorProvider.requireActor();

		LambdaUpdateWrapper<PageLocaleDO> update = versionedUpdate(
				siteId, pageId, localeId, request.getLockVersion(), currentUser)
				.set(PageLocaleDO::getTitle, request.getTitle().trim())
				.set(PageLocaleDO::getSummary, trimToNull(request.getSummary()))
				.set(PageLocaleDO::getSeoTitle, trimToNull(request.getSeoTitle()))
				.set(PageLocaleDO::getSeoDescription, trimToNull(request.getSeoDescription()))
				.set(PageLocaleDO::getMetadata, new HashMap<>(request.getMetadata()), "jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler");
		ensureUpdated(pageLocaleMapper.update(null, update));
		return PageLocaleVO.from(requirePageLocale(siteId, pageId, localeId));
	}

	@Override
	@Transactional
	public void deletePageLocale(UUID siteId, UUID pageId, UUID localeId, Integer lockVersion) {
		PageContext context = requireEditablePageContext(siteId, pageId);
		PageLocaleDO current = requireLockVersion(siteId, pageId, localeId, lockVersion);
		if (StringUtils.hasText(context.site().getDefaultLocale())
				&& context.site().getDefaultLocale().equalsIgnoreCase(current.getLocale())) {
			throw badRequest("默认语言不能删除，请先更换站点默认语言");
		}
		CmsActor currentUser = actorProvider.requireActor();
		LambdaUpdateWrapper<PageLocaleDO> update = versionedUpdate(
				siteId, pageId, localeId, lockVersion, currentUser)
				.set(PageLocaleDO::getDeletedAt, OffsetDateTime.now());
		ensureUpdated(pageLocaleMapper.update(null, update));
	}

	private PageContext requireEditablePageContext(UUID siteId, UUID pageId) {
		PageContext context = requirePageContext(siteId, pageId);
		if (context.page().getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
			throw badRequest("已归档页面不能编辑语言版本");
		}
		return context;
	}

	private PageContext requirePageContext(UUID siteId, UUID pageId) {
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
		return new PageContext(site, page);
	}

	private PageLocaleDO requirePageLocale(UUID siteId, UUID pageId, UUID localeId) {
		PageLocaleDO pageLocale = pageLocaleMapper.selectOne(new LambdaQueryWrapper<PageLocaleDO>()
				.eq(PageLocaleDO::getId, localeId)
				.eq(PageLocaleDO::getSiteId, siteId)
				.eq(PageLocaleDO::getPageId, pageId));
		if (pageLocale == null) {
			throw new CmsException(CmsErrorCode.NOT_FOUND, "页面语言版本不存在");
		}
		return pageLocale;
	}

	private PageLocaleDO requireLockVersion(
			UUID siteId, UUID pageId, UUID localeId, Integer lockVersion) {
		if (lockVersion == null) {
			throw badRequest("lockVersion 不能为空");
		}
		PageLocaleDO current = requirePageLocale(siteId, pageId, localeId);
		if (!lockVersion.equals(current.getLockVersion())) {
			throw conflict("语言版本已被其他操作更新，请刷新后重试");
		}
		return current;
	}

	private LambdaUpdateWrapper<PageLocaleDO> versionedUpdate(
			UUID siteId,
			UUID pageId,
			UUID localeId,
			Integer lockVersion,
			CmsActor currentUser) {
		return new LambdaUpdateWrapper<PageLocaleDO>()
				.eq(PageLocaleDO::getId, localeId)
				.eq(PageLocaleDO::getSiteId, siteId)
				.eq(PageLocaleDO::getPageId, pageId)
				.eq(PageLocaleDO::getLockVersion, lockVersion)
				.set(PageLocaleDO::getUpdatedBy, currentUser.userId())
				.set(PageLocaleDO::getUpdatedByType, actorType(currentUser))
				.set(PageLocaleDO::getUpdatedAt, OffsetDateTime.now())
				.set(PageLocaleDO::getLockVersion, lockVersion + 1);
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

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String actorType(CmsActor user) {
		return user.auditType();
	}

	private void ensureUpdated(int affectedRows) {
		if (affectedRows != 1) {
			throw conflict("语言版本已被其他操作更新，请刷新后重试");
		}
	}

	private CmsException localeConflict(String locale) {
		return conflict("该页面已存在 " + locale + " 语言版本");
	}

	private CmsException conflict(String message) {
		return new CmsException(CmsErrorCode.CONFLICT, message);
	}

	private CmsException badRequest(String message) {
		return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
	}

	private record PageContext(SiteDO site, PageDO page) {
	}
}
