package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.page.CmsPageService;
import com.systand.cms.api.page.CreatePageCommand;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.PageVO;
import com.systand.cms.api.page.PublishPageCommand;
import com.systand.cms.api.page.SchedulePageCommand;
import com.systand.cms.api.page.UpdatePageCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageConverter;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsPageService implements CmsPageService {
    private static final String PAGE_RESOURCE = "PAGE";
    private static final String PAGE_SECTION_RESOURCE = "PAGE_SECTION";

    private final CmsPageRepository pageRepository;
    private final CmsPageLocaleRepository pageLocaleRepository;
    private final CmsPageSectionRepository pageSectionRepository;
    private final CmsPageSectionLocaleRepository pageSectionLocaleRepository;
    private final CmsSiteRepository siteRepository;
    private final CmsActorProvider actorProvider;
    private final CmsMediaPort mediaPort;
    private final CmsTenantProvider tenantProvider;
    private final PageSectionTypeDefinitionService sectionTypeService;
    private final CmsPageConverter pageConverter;

    @Override
    public List<PageVO> getPages(UUID siteId, PagePublicationStatus status, PageKind pageKind, String keyword) {
        UUID tenantId = tenantProvider.requireTenantId();
        validateSite(tenantId, siteId);
        return pageConverter.toVOs(pageRepository.findAll(tenantId, siteId, status, pageKind, keyword));
    }

    @Override
    public PageVO getPage(UUID siteId, UUID pageId) {
        return pageConverter.toVO(requirePage(tenantProvider.requireTenantId(), siteId, pageId));
    }

    @Override
    @Transactional
    public PageVO createPage(UUID siteId, CreatePageCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        validateSite(tenantId, siteId);
        validateHomeRoute(command.home(), command.routePath());
        CmsActor actor = actorProvider.requireActor();
        PageState page = new PageState();
        page.setTenantId(tenantId);
        page.setSiteId(siteId);
        page.setCode(command.code());
        page.setRoutePath(command.routePath());
        page.setPageKind(command.pageKind() == null ? PageKind.STANDARD : command.pageKind());
        page.setTemplateCode(command.templateCode());
        page.setPublicationStatus(PagePublicationStatus.DRAFT);
        page.setHome(Boolean.TRUE.equals(command.home()));
        page.setSortOrder(command.sortOrder() == null ? 0 : command.sortOrder());
        page.setLayoutSettings(command.layoutSettings() == null
                ? new HashMap<>() : new HashMap<>(command.layoutSettings()));
        page.setLockVersion(1);
        page.setCreatedBy(actor.userId());
        page.setCreatedByType(actor.auditType());
        page.setUpdatedBy(actor.userId());
        page.setUpdatedByType(actor.auditType());
        try {
            return pageConverter.toVO(pageRepository.insert(page));
        } catch (DuplicateKeyException exception) {
            throw pageConflict();
        }
    }

    @Override
    @Transactional
    public PageVO updatePage(UUID siteId, UUID pageId, UpdatePageCommand command) {
        PageState page = requireLockVersion(siteId, pageId, command.lockVersion());
        if (page.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
            throw badRequest("已归档页面不能直接编辑");
        }
        validateHomeRoute(command.home(), command.routePath());
        int expectedVersion = page.getLockVersion();
        page.setCode(command.code());
        page.setRoutePath(command.routePath());
        page.setPageKind(command.pageKind());
        page.setTemplateCode(command.templateCode());
        page.setHome(command.home());
        page.setSortOrder(command.sortOrder());
        page.setLayoutSettings(new HashMap<>(command.layoutSettings()));
        touch(page, expectedVersion);
        try {
            ensureUpdated(pageRepository.update(page, expectedVersion));
        } catch (DuplicateKeyException exception) {
            throw pageConflict();
        }
        return getPage(siteId, pageId);
    }

    @Override
    @Transactional
    public void deletePage(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        if (page.getPublicationStatus() == PagePublicationStatus.PUBLISHED) {
            throw badRequest("已发布页面必须先下线或归档后才能删除");
        }
        UUID tenantId = page.getTenantId();
        OffsetDateTime deletedAt = OffsetDateTime.now();
        List<PageSectionState> sections = pageSectionRepository.findAll(
                tenantId, siteId, pageId, null, null);
        CmsActor actor = actorProvider.requireActor();
        page.setDeletedAt(deletedAt);
        touch(page, lockVersion);
        ensureUpdated(pageRepository.update(page, lockVersion));
        pageLocaleRepository.softDeleteByPage(
                tenantId, siteId, pageId, deletedAt, actor.userId(), actor.auditType());
        pageSectionLocaleRepository.softDeleteByPage(
                tenantId, siteId, pageId, deletedAt, actor.userId(), actor.auditType());
        pageSectionRepository.softDeleteByPage(
                tenantId, siteId, pageId, deletedAt, actor.userId(), actor.auditType());
        mediaPort.deleteResourceBindings(tenantId, PAGE_RESOURCE, pageId);
        sections.forEach(section -> mediaPort.deleteResourceBindings(
                tenantId, PAGE_SECTION_RESOURCE, section.getId()));
    }

    @Override
    @Transactional
    public PageVO submitForReview(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        requireStatus(page, PagePublicationStatus.DRAFT, PagePublicationStatus.UNPUBLISHED);
        page.setPublicationStatus(PagePublicationStatus.IN_REVIEW);
        page.setScheduledPublishAt(null);
        page.setScheduledUnpublishAt(null);
        return saveAndReload(page, lockVersion);
    }

    @Override
    @Transactional
    public PageVO withdrawReview(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        requireStatus(page, PagePublicationStatus.IN_REVIEW);
        page.setPublicationStatus(draftStatus(page));
        return saveAndReload(page, lockVersion);
    }

    @Override
    @Transactional
    public PageVO schedulePage(UUID siteId, UUID pageId, SchedulePageCommand command) {
        PageState page = requireLockVersion(siteId, pageId, command.lockVersion());
        requireStatus(page, PagePublicationStatus.DRAFT,
                PagePublicationStatus.IN_REVIEW, PagePublicationStatus.UNPUBLISHED);
        validateScheduleWindow(command.scheduledPublishAt(), command.scheduledUnpublishAt());
        page.setPublicationStatus(PagePublicationStatus.SCHEDULED);
        page.setScheduledPublishAt(command.scheduledPublishAt());
        page.setScheduledUnpublishAt(command.scheduledUnpublishAt());
        return saveAndReload(page, command.lockVersion());
    }

    @Override
    @Transactional
    public PageVO cancelSchedule(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        requireStatus(page, PagePublicationStatus.SCHEDULED);
        page.setPublicationStatus(draftStatus(page));
        page.setScheduledPublishAt(null);
        page.setScheduledUnpublishAt(null);
        return saveAndReload(page, lockVersion);
    }

    @Override
    @Transactional
    public PageVO publishPage(UUID siteId, UUID pageId, PublishPageCommand command) {
        PageState page = requireLockVersion(siteId, pageId, command.lockVersion());
        requireStatus(page, PagePublicationStatus.DRAFT, PagePublicationStatus.IN_REVIEW,
                PagePublicationStatus.SCHEDULED, PagePublicationStatus.UNPUBLISHED);
        validateSectionMediaForPublication(page.getTenantId(), siteId, pageId);
        OffsetDateTime publishedAt = OffsetDateTime.now();
        OffsetDateTime scheduledUnpublishAt = command.scheduledUnpublishAt();
        if (scheduledUnpublishAt == null && page.getPublicationStatus() == PagePublicationStatus.SCHEDULED) {
            scheduledUnpublishAt = page.getScheduledUnpublishAt();
        }
        if (scheduledUnpublishAt != null && !scheduledUnpublishAt.isAfter(publishedAt)) {
            throw badRequest("scheduledUnpublishAt 必须晚于实际发布时间");
        }
        page.setPublicationStatus(PagePublicationStatus.PUBLISHED);
        page.setPublishedAt(publishedAt);
        page.setUnpublishedAt(null);
        page.setScheduledUnpublishAt(scheduledUnpublishAt);
        if (page.getFirstPublishedAt() == null) {
            page.setFirstPublishedAt(publishedAt);
        }
        return saveAndReload(page, command.lockVersion());
    }

    @Override
    @Transactional
    public PageVO unpublishPage(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        requireStatus(page, PagePublicationStatus.PUBLISHED);
        page.setPublicationStatus(PagePublicationStatus.UNPUBLISHED);
        page.setUnpublishedAt(OffsetDateTime.now());
        return saveAndReload(page, lockVersion);
    }

    @Override
    @Transactional
    public PageVO archivePage(UUID siteId, UUID pageId, Integer lockVersion) {
        PageState page = requireLockVersion(siteId, pageId, lockVersion);
        if (page.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
            throw badRequest("页面已经归档");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (page.getPublicationStatus() == PagePublicationStatus.PUBLISHED) {
            page.setUnpublishedAt(now);
        }
        page.setPublicationStatus(PagePublicationStatus.ARCHIVED);
        page.setArchivedAt(now);
        return saveAndReload(page, lockVersion);
    }

    private PageVO saveAndReload(PageState page, int expectedVersion) {
        touch(page, expectedVersion);
        ensureUpdated(pageRepository.update(page, expectedVersion));
        return getPage(page.getSiteId(), page.getId());
    }

    private void touch(PageState page, int expectedVersion) {
        CmsActor actor = actorProvider.requireActor();
        page.setUpdatedAt(OffsetDateTime.now());
        page.setUpdatedBy(actor.userId());
        page.setUpdatedByType(actor.auditType());
        page.setLockVersion(expectedVersion + 1);
    }

    private void validateSite(UUID tenantId, UUID siteId) {
        if (!siteRepository.existsActive(tenantId, siteId)) {
            throw new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用");
        }
    }

    private PageState requirePage(UUID tenantId, UUID siteId, UUID pageId) {
        return pageRepository.findById(tenantId, siteId, pageId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在"));
    }

    private PageState requireLockVersion(UUID siteId, UUID pageId, Integer lockVersion) {
        if (lockVersion == null) {
            throw badRequest("lockVersion 不能为空");
        }
        PageState page = requirePage(tenantProvider.requireTenantId(), siteId, pageId);
        if (!lockVersion.equals(page.getLockVersion())) {
            throw new CmsException(CmsErrorCode.CONFLICT, "页面已被其他操作更新，请刷新后重试");
        }
        return page;
    }

    private void ensureUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw new CmsException(CmsErrorCode.CONFLICT, "页面已被其他操作更新，请刷新后重试");
        }
    }

    private void validateScheduleWindow(OffsetDateTime publishAt, OffsetDateTime unpublishAt) {
        if (unpublishAt != null && !unpublishAt.isAfter(publishAt)) {
            throw badRequest("scheduledUnpublishAt 必须晚于 scheduledPublishAt");
        }
    }

    private void validateSectionMediaForPublication(UUID tenantId, UUID siteId, UUID pageId) {
        List<PageSectionState> sections = pageSectionRepository.findAll(tenantId, siteId, pageId, true, null);
        for (PageSectionState section : sections) {
            PageSectionTypeDefinition sectionType = sectionTypeService.requireAvailableDefinition(
                    section.getSectionType(), section.getSchemaVersion());
            if (sectionType.requiredAnyOfMediaRoles().isEmpty()) {
                continue;
            }
            var assets = mediaPort.findBindings(tenantId, PAGE_SECTION_RESOURCE, section.getId(), null);
            boolean hasRequiredMedia = assets.stream()
                    .anyMatch(asset -> sectionType.requiredAnyOfMediaRoles().contains(asset.role()));
            if (!hasRequiredMedia) {
                throw badRequest("区块 " + section.getSectionKey() + " 至少需要一个媒体角色: "
                        + String.join(" 或 ", sectionType.requiredAnyOfMediaRoles()));
            }
        }
    }

    private void validateHomeRoute(Boolean home, String routePath) {
        if (Boolean.TRUE.equals(home) != "/".equals(routePath)) {
            throw badRequest("首页必须使用 / 路由，/ 路由也必须设置 home=true");
        }
    }

    private void requireStatus(PageState page, PagePublicationStatus... allowedStatuses) {
        for (PagePublicationStatus allowedStatus : allowedStatuses) {
            if (page.getPublicationStatus() == allowedStatus) {
                return;
            }
        }
        throw badRequest("当前页面状态不允许执行此操作: " + page.getPublicationStatus().toValue());
    }

    private PagePublicationStatus draftStatus(PageState page) {
        return page.getPublishedAt() == null ? PagePublicationStatus.DRAFT : PagePublicationStatus.UNPUBLISHED;
    }

    private CmsException pageConflict() {
        return new CmsException(CmsErrorCode.CONFLICT, "同一站点下的页面编码、路由或首页设置重复");
    }

    private CmsException badRequest(String message) {
        return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
    }
}
