package com.systand.cms.application.site.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.site.CmsCreateSiteCommand;
import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.api.site.CmsSiteOperations;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsSiteOperations implements CmsSiteOperations {
	private final SiteMapper siteMapper;
	private final CmsActorProvider actorProvider;
	private final CmsTenantProvider tenantProvider;
	private final CmsSiteVOMapper siteVOMapper;

	/**
	 * Get all sites by current tenant
	 */
	@Override
	public List<CmsSiteVO> getSitesByTenant() {
		return siteVOMapper.toVOs(siteMapper.selectList(new LambdaQueryWrapper<SiteDO>()
				.eq(SiteDO::getTenantId, tenantProvider.requireTenantId())));
	}

	@Transactional
	@Override
	public CmsSiteVO createSite(CmsCreateSiteCommand request) {
		UUID actorId = actorProvider.requireActor().userId();
		if (actorId == null) {
			throw new CmsException(CmsErrorCode.INVALID_ARGUMENT,
					"创建站点需要用户身份；当前数据库约束不允许系统或匿名操作者");
		}
		List<String> locales = request.enabledLocales() == null || request.enabledLocales().isEmpty()
				? List.of(request.defaultLocale()) : List.copyOf(request.enabledLocales());
		if (!locales.contains(request.defaultLocale())) {
			throw new CmsException(CmsErrorCode.INVALID_ARGUMENT, "默认语言必须包含在启用语言中");
		}
		SiteDO site = new SiteDO();
		site.setTenantId(tenantProvider.requireTenantId());
		site.setCode(request.code());
		site.setName(request.name());
		site.setDefaultLocale(request.defaultLocale());
		site.setEnabledLocales(locales);
		site.setStatus("ACTIVE");
		site.setCreatedBy(actorId);
		site.setUpdatedBy(actorId);
		try {
			siteMapper.insert(site);
		} catch (DuplicateKeyException exception) {
			throw new CmsException(CmsErrorCode.CONFLICT, "当前租户已存在相同站点编码");
		}
		return siteVOMapper.toVO(siteMapper.selectOne(new LambdaQueryWrapper<SiteDO>()
				.eq(SiteDO::getId, site.getId())
				.eq(SiteDO::getTenantId, site.getTenantId())));
	}
}
