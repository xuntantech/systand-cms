package com.systand.cms.application.site.service;

import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.site.CmsCreateSiteForTenantCommand;
import com.systand.cms.api.site.CmsSiteAdministrationService;
import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.application.site.converter.CmsSiteConverter;
import com.systand.cms.application.site.port.CmsSiteAdministrationRepository;
import com.systand.cms.core.error.CmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsSiteAdministrationService implements CmsSiteAdministrationService {
    private final CmsSiteAdministrationRepository repository;
    private final CmsActorProvider actorProvider;
    private final CmsSiteConverter converter;
    private final CmsSiteFactory siteFactory;

    @Override
    public List<CmsSiteVO> getAllSites() {
        return converter.toVOs(repository.findAll());
    }

    @Override
    public List<CmsSiteVO> getSitesByTenantId(UUID tenantId) {
        return converter.toVOs(repository.findAllByTenantId(tenantId));
    }

    @Override
    public Optional<CmsSiteVO> getSiteById(UUID siteId) {
        return repository.findById(siteId).map(converter::toVO);
    }

    @Override
    @Transactional
    public CmsSiteVO createSite(CmsCreateSiteForTenantCommand command) {
        var site = siteFactory.create(
                command.tenantId(), actorProvider.requireActor().userId(),
                command.code(), command.name(), command.defaultLocale(),
                command.enabledLocales(), command.status());
        try {
            return converter.toVO(repository.insert(site));
        } catch (DuplicateKeyException exception) {
            throw new CmsException(CmsErrorCode.CONFLICT, "目标租户已存在相同站点编码");
        }
    }
}
