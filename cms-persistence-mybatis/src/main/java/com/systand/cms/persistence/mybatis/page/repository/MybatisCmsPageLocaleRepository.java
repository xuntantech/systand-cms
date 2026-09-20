package com.systand.cms.persistence.mybatis.page.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.application.page.model.PageLocaleState;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
import com.systand.cms.persistence.mybatis.page.converter.CmsPageLocalePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsPageLocaleRepository implements CmsPageLocaleRepository {
    private final PageLocaleMapper mapper;
    private final CmsPageLocalePersistenceConverter converter;

    @Override
    public List<PageLocaleState> findAll(UUID tenantId, UUID siteId, UUID pageId) {
        return converter.toStates(mapper.selectList(new LambdaQueryWrapper<PageLocaleDO>()
                .eq(PageLocaleDO::getTenantId, tenantId)
                .eq(PageLocaleDO::getSiteId, siteId)
                .eq(PageLocaleDO::getPageId, pageId)
                .orderByAsc(PageLocaleDO::getLocale)));
    }

    @Override
    public Optional<PageLocaleState> findById(UUID tenantId, UUID siteId, UUID pageId, UUID localeId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PageLocaleDO>()
                        .eq(PageLocaleDO::getTenantId, tenantId)
                        .eq(PageLocaleDO::getSiteId, siteId)
                        .eq(PageLocaleDO::getPageId, pageId)
                        .eq(PageLocaleDO::getId, localeId)))
                .map(converter::toState);
    }

    @Override
    public PageLocaleState insert(PageLocaleState locale) {
        PageLocaleDO dataObject = converter.toDO(locale);
        mapper.insert(dataObject);
        return findById(locale.getTenantId(), locale.getSiteId(), locale.getPageId(), dataObject.getId())
                .orElseGet(() -> converter.toState(dataObject));
    }

    @Override
    public int update(PageLocaleState locale, int expectedLockVersion) {
        return mapper.update(null, new LambdaUpdateWrapper<PageLocaleDO>()
                .eq(PageLocaleDO::getTenantId, locale.getTenantId())
                .eq(PageLocaleDO::getSiteId, locale.getSiteId())
                .eq(PageLocaleDO::getPageId, locale.getPageId())
                .eq(PageLocaleDO::getId, locale.getId())
                .eq(PageLocaleDO::getLockVersion, expectedLockVersion)
                .set(PageLocaleDO::getTitle, locale.getTitle())
                .set(PageLocaleDO::getSummary, locale.getSummary())
                .set(PageLocaleDO::getSeoTitle, locale.getSeoTitle())
                .set(PageLocaleDO::getSeoDescription, locale.getSeoDescription())
                .set(PageLocaleDO::getMetadata, locale.getMetadata(),
                        "jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler")
                .set(PageLocaleDO::getDeletedAt, locale.getDeletedAt())
                .set(PageLocaleDO::getUpdatedAt, locale.getUpdatedAt())
                .set(PageLocaleDO::getUpdatedBy, locale.getUpdatedBy())
                .set(PageLocaleDO::getUpdatedByType, locale.getUpdatedByType())
                .set(PageLocaleDO::getLockVersion, locale.getLockVersion()));
    }

    @Override
    public int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                                UUID actorId, String actorType) {
        return mapper.update(null, new LambdaUpdateWrapper<PageLocaleDO>()
                .eq(PageLocaleDO::getTenantId, tenantId)
                .eq(PageLocaleDO::getSiteId, siteId)
                .eq(PageLocaleDO::getPageId, pageId)
                .set(PageLocaleDO::getDeletedAt, deletedAt)
                .set(PageLocaleDO::getUpdatedAt, deletedAt)
                .set(PageLocaleDO::getUpdatedBy, actorId)
                .set(PageLocaleDO::getUpdatedByType, actorType)
                .setSql("lock_version = lock_version + 1"));
    }
}
