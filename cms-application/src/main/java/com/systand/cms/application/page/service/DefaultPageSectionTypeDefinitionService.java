package com.systand.cms.application.page.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.port.CmsPageSectionTypeRepository;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultPageSectionTypeDefinitionService implements PageSectionTypeDefinitionService {
    private final CmsPageSectionTypeRepository repository;
    private final CmsTenantProvider tenantProvider;

    @Override
    @Transactional
    public List<PageSectionTypeDefinition> getAvailableDefinitions() {
        UUID tenantId = tenantProvider.requireTenantId();
        repository.initializeBuiltIns(tenantId);
        return repository.findAvailable(tenantId, null, null);
    }

    @Override
    @Transactional
    public PageSectionTypeDefinition requireAvailableDefinition(String code, Integer schemaVersion) {
        if (!StringUtils.hasText(code) || schemaVersion == null || schemaVersion < 1) {
            throw badRequest("sectionType 和 schemaVersion 不能为空");
        }
        UUID tenantId = tenantProvider.requireTenantId();
        repository.initializeBuiltIns(tenantId);
        String normalizedCode = code.trim().toLowerCase(Locale.ROOT);
        return repository.findAvailable(tenantId, normalizedCode, schemaVersion).stream()
                .findFirst()
                .orElseThrow(() -> badRequest("当前租户不可使用区块类型: "
                        + normalizedCode + "@" + schemaVersion));
    }

    private CmsException badRequest(String message) {
        return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
    }
}
