package com.systand.cms.persistence.mybatis.site.repository;

import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteAdministrationRepository;
import com.systand.cms.persistence.mybatis.site.converter.CmsSitePersistenceConverter;
import com.systand.cms.persistence.mybatis.site.mapper.SiteAdministrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsSiteAdministrationRepository implements CmsSiteAdministrationRepository {
    private final SiteAdministrationMapper mapper;
    private final CmsSitePersistenceConverter converter;

    @Override
    public List<CmsSiteState> findAll() {
        return converter.toStates(mapper.selectAllSites());
    }

    @Override
    public List<CmsSiteState> findAllByTenantId(UUID tenantId) {
        return converter.toStates(mapper.selectSitesByTenantId(tenantId));
    }

    @Override
    public Optional<CmsSiteState> findById(UUID siteId) {
        return Optional.ofNullable(mapper.selectSiteById(siteId)).map(converter::toState);
    }

    @Override
    public CmsSiteState insert(CmsSiteState site) {
        var dataObject = converter.toDO(site);
        mapper.insert(dataObject);
        return Optional.ofNullable(mapper.selectSiteById(dataObject.getId()))
                .map(converter::toState)
                .orElseGet(() -> converter.toState(dataObject));
    }
}
