package com.systand.cms.persistence.mybatis.page.section.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.persistence.mybatis.page.section.converter.CmsPageSectionPersistenceConverter;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsPageSectionRepository implements CmsPageSectionRepository {
    private final PageSectionMapper mapper;
    private final CmsPageSectionPersistenceConverter converter;

    @Override
    public List<PageSectionState> findAll(UUID tenantId, UUID siteId, UUID pageId,
                                          Boolean visible, String sectionType) {
        return converter.toStates(mapper.selectList(new LambdaQueryWrapper<PageSectionDO>()
                .eq(PageSectionDO::getTenantId, tenantId)
                .eq(PageSectionDO::getSiteId, siteId)
                .eq(PageSectionDO::getPageId, pageId)
                .eq(visible != null, PageSectionDO::getIsVisible, visible)
                .eq(StringUtils.hasText(sectionType), PageSectionDO::getSectionType,
                        StringUtils.hasText(sectionType) ? sectionType.trim() : null)
                .orderByAsc(PageSectionDO::getSortOrder)
                .orderByAsc(PageSectionDO::getId)));
    }

    @Override
    public Optional<PageSectionState> findById(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PageSectionDO>()
                        .eq(PageSectionDO::getTenantId, tenantId)
                        .eq(PageSectionDO::getSiteId, siteId)
                        .eq(PageSectionDO::getPageId, pageId)
                        .eq(PageSectionDO::getId, sectionId)))
                .map(converter::toState);
    }

    @Override
    public PageSectionState insert(PageSectionState section) {
        PageSectionDO dataObject = converter.toDO(section);
        mapper.insert(dataObject);
        return findById(section.getTenantId(), section.getSiteId(), section.getPageId(), dataObject.getId())
                .orElseGet(() -> converter.toState(dataObject));
    }

    @Override
    public int update(PageSectionState section, int expectedLockVersion) {
        return mapper.update(null, new LambdaUpdateWrapper<PageSectionDO>()
                .eq(PageSectionDO::getTenantId, section.getTenantId())
                .eq(PageSectionDO::getSiteId, section.getSiteId())
                .eq(PageSectionDO::getPageId, section.getPageId())
                .eq(PageSectionDO::getId, section.getId())
                .eq(PageSectionDO::getLockVersion, expectedLockVersion)
                .set(PageSectionDO::getSectionKey, section.getSectionKey())
                .set(PageSectionDO::getSectionType, section.getSectionType())
                .set(PageSectionDO::getAdminLabel, section.getAdminLabel())
                .set(PageSectionDO::getAnchorId, section.getAnchorId())
                .set(PageSectionDO::getSortOrder, section.getSortOrder())
                .set(PageSectionDO::getIsVisible, section.getVisible())
                .set(PageSectionDO::getSettings, section.getSettings(),
                        "jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler")
                .set(PageSectionDO::getSchemaVersion, section.getSchemaVersion())
                .set(PageSectionDO::getDeletedAt, section.getDeletedAt())
                .set(PageSectionDO::getUpdatedAt, section.getUpdatedAt())
                .set(PageSectionDO::getUpdatedBy, section.getUpdatedBy())
                .set(PageSectionDO::getUpdatedByType, section.getUpdatedByType())
                .set(PageSectionDO::getLockVersion, section.getLockVersion()));
    }

    @Override
    public int softDeleteByPage(UUID tenantId, UUID siteId, UUID pageId, OffsetDateTime deletedAt,
                                UUID actorId, String actorType) {
        return mapper.update(null, new LambdaUpdateWrapper<PageSectionDO>()
                .eq(PageSectionDO::getTenantId, tenantId)
                .eq(PageSectionDO::getSiteId, siteId)
                .eq(PageSectionDO::getPageId, pageId)
                .set(PageSectionDO::getDeletedAt, deletedAt)
                .set(PageSectionDO::getUpdatedAt, deletedAt)
                .set(PageSectionDO::getUpdatedBy, actorId)
                .set(PageSectionDO::getUpdatedByType, actorType)
                .setSql("lock_version = lock_version + 1"));
    }
}
