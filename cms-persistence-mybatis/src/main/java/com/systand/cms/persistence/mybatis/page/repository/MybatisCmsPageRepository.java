package com.systand.cms.persistence.mybatis.page.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.persistence.mybatis.page.converter.CmsPagePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsPageRepository implements CmsPageRepository {
    private final PageMapper mapper;
    private final CmsPagePersistenceConverter converter;

    @Override
    public List<PageState> findAll(UUID tenantId, UUID siteId, PagePublicationStatus status,
                                   PageKind kind, String keyword) {
        LambdaQueryWrapper<PageDO> query = new LambdaQueryWrapper<PageDO>()
                .eq(PageDO::getTenantId, tenantId)
                .eq(PageDO::getSiteId, siteId)
                .eq(status != null, PageDO::getPublicationStatus, status)
                .eq(kind != null, PageDO::getPageKind, kind)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(PageDO::getCode, keyword == null ? null : keyword.trim())
                        .or()
                        .like(PageDO::getRoutePath, keyword == null ? null : keyword.trim()))
                .orderByDesc(PageDO::getIsHome)
                .orderByAsc(PageDO::getSortOrder)
                .orderByDesc(PageDO::getUpdatedAt);
        return converter.toStates(mapper.selectList(query));
    }

    @Override
    public Optional<PageState> findById(UUID tenantId, UUID siteId, UUID pageId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<PageDO>()
                        .eq(PageDO::getTenantId, tenantId)
                        .eq(PageDO::getSiteId, siteId)
                        .eq(PageDO::getId, pageId)))
                .map(converter::toState);
    }

    @Override
    public PageState insert(PageState page) {
        PageDO dataObject = converter.toDO(page);
        mapper.insert(dataObject);
        return findById(page.getTenantId(), page.getSiteId(), dataObject.getId())
                .orElseGet(() -> converter.toState(dataObject));
    }

    @Override
    public int update(PageState page, int expectedLockVersion) {
        return mapper.update(null, new LambdaUpdateWrapper<PageDO>()
                .eq(PageDO::getTenantId, page.getTenantId())
                .eq(PageDO::getSiteId, page.getSiteId())
                .eq(PageDO::getId, page.getId())
                .eq(PageDO::getLockVersion, expectedLockVersion)
                .set(PageDO::getCode, page.getCode())
                .set(PageDO::getRoutePath, page.getRoutePath())
                .set(PageDO::getPageKind, page.getPageKind())
                .set(PageDO::getTemplateCode, page.getTemplateCode())
                .set(PageDO::getPublicationStatus, page.getPublicationStatus())
                .set(PageDO::getIsHome, page.getHome())
                .set(PageDO::getSortOrder, page.getSortOrder())
                .set(PageDO::getLayoutSettings, page.getLayoutSettings(),
                        "jdbcType=OTHER,typeHandler=com.systand.cms.persistence.mybatis.CmsJsonTypeHandler")
                .set(PageDO::getScheduledPublishAt, page.getScheduledPublishAt())
                .set(PageDO::getScheduledUnpublishAt, page.getScheduledUnpublishAt())
                .set(PageDO::getPublishedAt, page.getPublishedAt())
                .set(PageDO::getFirstPublishedAt, page.getFirstPublishedAt())
                .set(PageDO::getUnpublishedAt, page.getUnpublishedAt())
                .set(PageDO::getArchivedAt, page.getArchivedAt())
                .set(PageDO::getDeletedAt, page.getDeletedAt())
                .set(PageDO::getUpdatedAt, page.getUpdatedAt())
                .set(PageDO::getUpdatedBy, page.getUpdatedBy())
                .set(PageDO::getUpdatedByType, page.getUpdatedByType())
                .set(PageDO::getLockVersion, page.getLockVersion()));
    }
}
