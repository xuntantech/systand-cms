package com.systand.cms.application.site.service;

import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.site.CmsCreateSiteCommand;
import com.systand.cms.api.site.CmsSiteService;
import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.site.converter.CmsSiteConverter;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsSiteService implements CmsSiteService {
    private final CmsSiteRepository siteRepository;
    private final CmsActorProvider actorProvider;
    private final CmsTenantProvider tenantProvider;
    private final CmsSiteConverter siteConverter;
    private final CmsSiteFactory siteFactory;

    @Override
    public List<CmsSiteVO> getSitesByTenant() {
        return siteConverter.toVOs(siteRepository.findAllByTenantId(tenantProvider.requireTenantId()));
    }

    @Override
    public CmsSiteVO getSite(UUID siteId) {
        UUID tenantId = tenantProvider.requireTenantId();
        return siteRepository.findById(tenantId, siteId)
                .map(siteConverter::toVO)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "CMS 站点不存在"));
    }

    @Override
    @Transactional
    public CmsSiteVO createSite(CmsCreateSiteCommand command) {
        CmsSiteState site = siteFactory.create(
                tenantProvider.requireTenantId(), actorProvider.requireActor().userId(),
                command.code(), command.name(), command.defaultLocale(),
                command.enabledLocales(), "ACTIVE");
        try {
            return siteConverter.toVO(siteRepository.insert(site));
        } catch (DuplicateKeyException exception) {
            throw new CmsException(CmsErrorCode.CONFLICT, "当前租户已存在相同站点编码");
        }
    }
}
