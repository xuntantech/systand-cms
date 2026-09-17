package com.systand.cms.application.page.section.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.core.error.CmsException;

import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeCatalogDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionTypeMapper;
import com.systand.cms.application.page.section.vo.PageSectionTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PageSectionTypeServiceImpl implements PageSectionTypeService {
	private final PageSectionTypeMapper pageSectionTypeMapper;
	private final CmsTenantProvider tenantProvider;

	@Override
	@Transactional
	public List<PageSectionTypeVO> getAvailableSectionTypes() {
		UUID tenantId = requireTenantId();
		initializeBuiltInsForTenant(tenantId);
		return pageSectionTypeMapper.selectAvailableForTenant(tenantId, null, null).stream()
				.map(PageSectionTypeDefinition::from)
				.map(PageSectionTypeVO::from)
				.toList();
	}

	@Override
	@Transactional
	public PageSectionTypeDefinition requireAvailableDefinition(String code, Integer schemaVersion) {
		if (!StringUtils.hasText(code) || schemaVersion == null || schemaVersion < 1) {
			throw badRequest("sectionType 和 schemaVersion 不能为空");
		}
		UUID tenantId = requireTenantId();
		initializeBuiltInsForTenant(tenantId);
		String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
		List<PageSectionTypeCatalogDO> matches = pageSectionTypeMapper.selectAvailableForTenant(
				tenantId, normalizedCode, schemaVersion);
		if (matches.isEmpty()) {
			throw badRequest("当前租户不可使用区块类型: "
					+ normalizedCode + "@" + schemaVersion);
		}
		return PageSectionTypeDefinition.from(matches.getFirst());
	}

	@Override
	public void initializeBuiltInsForTenant(UUID tenantId) {
		if (tenantId == null) {
			throw badRequest("租户ID不能为空");
		}
		pageSectionTypeMapper.initializeBuiltInsForTenant(tenantId);
	}

	private UUID requireTenantId() {
		return tenantProvider.requireTenantId();
	}

	private CmsException badRequest(String message) {
		return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
	}
}
