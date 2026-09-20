package com.systand.cms.persistence.mybatis.page.section.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.application.page.model.PageSectionLocaleState;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.persistence.mybatis.page.section.converter.CmsPageSectionLocalePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsPageSectionLocaleRepository implements CmsPageSectionLocaleRepository {
    private final PageSectionLocaleMapper mapper;
    private final CmsPageSectionLocalePersistenceConverter converter;

    @Override
    public List<PageSectionLocaleState> findAll(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId) {
        return converter.toStates(mapper.selectList(new LambdaQueryWrapper<PageSectionLocaleDO>()
                .eq(PageSectionLocaleDO::getTenantId, tenantId)
                .eq(PageSectionLocaleDO::getSiteId, siteId)
                .eq(PageSectionLocaleDO::getPageId, pageId)
                .eq(PageSectionLocaleDO::getSectionId, sectionId)
                .orderByAsc(PageSectionLocaleDO::getLocale)));
    }

    @Override
    public Optional<PageSectionLocaleState> findById(UUID tenantId, UUID siteId, UUID pageId,
                                                     UUID sectionId, UUID localeId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PageSectionLocaleDO>()
                        .eq(PageSectionLocaleDO::getTenantId, tenantId)
                        .eq(PageSectionLocaleDO::getSiteId, siteId)
                        .eq(PageSectionLocaleDO::getPageId, pageId)
                        .eq(PageSectionLocaleDO::getSectionId, sectionId)
                        .eq(PageSectionLocaleDO::getId, localeId)))
                .map(converter::toState);
    }

    @Override
    public PageSectionLocaleState insert(PageSectionLocaleState locale) {
        PageSectionLocaleDO dataObject = converter.toDO(locale);
        mapper.insert(dataObject);
        return findById(locale.getTenantId(), locale.getSiteId(), locale.getPageId(),
                        locale.getSectionId(), dataObject.getId())
                .orElseGet(() -> converter.toState(dataObject));
    }

    @Override
    public int update(PageSectionLocaleState locale, int expectedLockVersion) {
        return mapper.update(null, new LambdaUpdateWrapper<PageSectionLocaleDO>()
                .eq(PageSectionLocaleDO::getTenantId, locale.getTenantId())
                .eq(PageSectionLocaleDO::getSiteId, locale.getSiteId())
                .eq(PageSectionLocaleDO::getPageId, locale.getPageId())
                .eq(PageSectionLocaleDO::getSectionId, locale.getSectionId())
                .eq(PageSectionLocaleDO::getId, locale.getId())
                .eq(PageSectionLocaleDO::getLockVersion, expectedLockVersion)
                .set(PageSectionLocaleDO::getTranslationStatus, locale.getTranslationStatus())
                .set(PageSectionLocaleDO::getContent, locale.getContent(),
                        "jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler")
                .set(PageSectionLocaleDO::getDeletedAt, locale.getDeletedAt())
                .set(PageSectionLocaleDO::getUpdatedAt, locale.getUpdatedAt())
                .set(PageSectionLocaleDO::getUpdatedBy, locale.getUpdatedBy())
                .set(PageSectionLocaleDO::getUpdatedByType, locale.getUpdatedByType())
                .set(PageSectionLocaleDO::getLockVersion, locale.getLockVersion()));
    }

    @Override
    public int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                                UUID actorId, String actorType) {
        return softDelete(new LambdaUpdateWrapper<PageSectionLocaleDO>()
                .eq(PageSectionLocaleDO::getTenantId, tenantId)
                .eq(PageSectionLocaleDO::getSiteId, siteId)
                .eq(PageSectionLocaleDO::getPageId, pageId), deletedAt, actorId, actorType);
    }

    @Override
    public int softDeleteBySection(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId,
                                   OffsetDateTime deletedAt, UUID actorId, String actorType) {
        return softDelete(new LambdaUpdateWrapper<PageSectionLocaleDO>()
                .eq(PageSectionLocaleDO::getTenantId, tenantId)
                .eq(PageSectionLocaleDO::getSiteId, siteId)
                .eq(PageSectionLocaleDO::getPageId, pageId)
                .eq(PageSectionLocaleDO::getSectionId, sectionId), deletedAt, actorId, actorType);
    }

    private int softDelete(LambdaUpdateWrapper<PageSectionLocaleDO> update, OffsetDateTime deletedAt,
                           UUID actorId, String actorType) {
        return mapper.update(null, update
                .set(PageSectionLocaleDO::getDeletedAt, deletedAt)
                .set(PageSectionLocaleDO::getUpdatedAt, deletedAt)
                .set(PageSectionLocaleDO::getUpdatedBy, actorId)
                .set(PageSectionLocaleDO::getUpdatedByType, actorType)
                .setSql("lock_version = lock_version + 1"));
    }
}
