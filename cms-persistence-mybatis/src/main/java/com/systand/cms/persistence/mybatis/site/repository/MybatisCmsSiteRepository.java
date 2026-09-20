package com.systand.cms.persistence.mybatis.site.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.persistence.mybatis.site.converter.CmsSitePersistenceConverter;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsSiteRepository implements CmsSiteRepository {
    private final SiteMapper mapper;
    private final CmsSitePersistenceConverter converter;

    @Override
    public List<CmsSiteState> findAllByTenantId(UUID tenantId) {
        return converter.toStates(mapper.selectList(new LambdaQueryWrapper<SiteDO>()
                .eq(SiteDO::getTenantId, tenantId)
                .orderByDesc(SiteDO::getCreatedAt)
                .orderByAsc(SiteDO::getId)));
    }

    @Override
    public Optional<CmsSiteState> findById(UUID tenantId, UUID siteId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<SiteDO>()
                        .eq(SiteDO::getTenantId, tenantId)
                        .eq(SiteDO::getId, siteId)))
                .map(converter::toState);
    }

    @Override
    public boolean existsActive(UUID tenantId, UUID siteId) {
        return mapper.exists(new LambdaQueryWrapper<SiteDO>()
                .eq(SiteDO::getTenantId, tenantId)
                .eq(SiteDO::getId, siteId)
                .eq(SiteDO::getStatus, "ACTIVE"));
    }

    @Override
    public CmsSiteState insert(CmsSiteState site) {
        SiteDO dataObject = converter.toDO(site);
        mapper.insert(dataObject);
        return findById(site.getTenantId(), dataObject.getId())
                .orElseGet(() -> converter.toState(dataObject));
    }
}
