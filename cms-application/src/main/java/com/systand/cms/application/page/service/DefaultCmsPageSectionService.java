package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.page.CmsPageSectionService;
import com.systand.cms.api.page.CreatePageSectionCommand;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.PageSectionVO;
import com.systand.cms.api.page.UpdatePageSectionCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageSectionConverter;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsPageSectionService implements CmsPageSectionService {
    private static final String PAGE_SECTION_RESOURCE = "PAGE_SECTION";

    private final CmsPageSectionRepository sectionRepository;
    private final CmsPageSectionLocaleRepository sectionLocaleRepository;
    private final CmsPageRepository pageRepository;
    private final CmsSiteRepository siteRepository;
    private final CmsActorProvider actorProvider;
    private final CmsMediaPort mediaPort;
    private final CmsTenantProvider tenantProvider;
    private final PageSectionTypeDefinitionService sectionTypeService;
    private final CmsPageSectionConverter converter;

    @Override
    public List<PageSectionVO> getSections(UUID siteId, UUID pageId, Boolean visible, String sectionType) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId, false);
        return converter.toVOs(sectionRepository.findAll(
                tenantId, siteId, pageId, visible, sectionType));
    }

    @Override
    public PageSectionVO getSection(UUID siteId, UUID pageId, UUID sectionId) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId, false);
        return converter.toVO(requireSection(tenantId, siteId, pageId, sectionId));
    }

    @Override
    @Transactional
    public PageSectionVO createSection(UUID siteId, UUID pageId, CreatePageSectionCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId, true);
        int schemaVersion = command.schemaVersion() == null ? 1 : command.schemaVersion();
        PageSectionTypeDefinition sectionType = sectionTypeService.requireAvailableDefinition(
                command.sectionType(), schemaVersion);
        CmsActor actor = actorProvider.requireActor();
        PageSectionState state = new PageSectionState();
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setPageId(pageId);
        state.setSectionKey(command.sectionKey().trim());
        state.setSectionType(sectionType.code());
        state.setAdminLabel(trimToNull(command.adminLabel()));
        state.setAnchorId(trimToNull(command.anchorId()));
        state.setSortOrder(command.sortOrder() == null ? 0 : command.sortOrder());
        state.setVisible(command.visible() == null || command.visible());
        state.setSettings(sectionType.mergeSettings(command.settings()));
        state.setSchemaVersion(schemaVersion);
        state.setLockVersion(1);
        state.setCreatedBy(actor.userId());
        state.setCreatedByType(actor.auditType());
        state.setUpdatedBy(actor.userId());
        state.setUpdatedByType(actor.auditType());
        try {
            return converter.toVO(sectionRepository.insert(state));
        } catch (DuplicateKeyException exception) {
            throw sectionConflict();
        }
    }

    @Override
    @Transactional
    public PageSectionVO updateSection(
            UUID siteId, UUID pageId, UUID sectionId, UpdatePageSectionCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId, true);
        PageSectionState state = requireLockVersion(
                tenantId, siteId, pageId, sectionId, command.lockVersion());
        PageSectionTypeDefinition sectionType = sectionTypeService.requireAvailableDefinition(
                command.sectionType(), command.schemaVersion());
        if (!state.getSectionType().equals(sectionType.code())) {
            throw badRequest("sectionType 创建后不能修改");
        }
        if (!state.getSectionKey().equals(command.sectionKey().trim())) {
            throw badRequest("sectionKey 创建后不能修改");
        }
        int expectedVersion = state.getLockVersion();
        state.setAdminLabel(trimToNull(command.adminLabel()));
        state.setAnchorId(trimToNull(command.anchorId()));
        state.setSortOrder(command.sortOrder());
        state.setVisible(command.visible());
        state.setSettings(sectionType.mergeSettings(command.settings()));
        state.setSchemaVersion(command.schemaVersion());
        touch(state, expectedVersion);
        try {
            ensureUpdated(sectionRepository.update(state, expectedVersion));
        } catch (DuplicateKeyException exception) {
            throw sectionConflict();
        }
        return converter.toVO(requireSection(tenantId, siteId, pageId, sectionId));
    }

    @Override
    @Transactional
    public void deleteSection(UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId, true);
        PageSectionState state = requireLockVersion(
                tenantId, siteId, pageId, sectionId, lockVersion);
        OffsetDateTime deletedAt = OffsetDateTime.now();
        CmsActor actor = actorProvider.requireActor();
        state.setDeletedAt(deletedAt);
        touch(state, lockVersion);
        ensureUpdated(sectionRepository.update(state, lockVersion));
        sectionLocaleRepository.softDeleteBySection(
                tenantId, siteId, pageId, sectionId, deletedAt, actor.userId(), actor.auditType());
        mediaPort.deleteResourceBindings(tenantId, PAGE_SECTION_RESOURCE, sectionId);
    }

    private PageState requirePageContext(UUID tenantId, UUID siteId, UUID pageId, boolean editable) {
        if (!siteRepository.existsActive(tenantId, siteId)) {
            throw new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用");
        }
        PageState page = pageRepository.findById(tenantId, siteId, pageId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在"));
        if (editable && page.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
            throw badRequest("已归档页面不能编辑区块");
        }
        return page;
    }

    private PageSectionState requireSection(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId) {
        return sectionRepository.findById(tenantId, siteId, pageId, sectionId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面区块不存在"));
    }

    private PageSectionState requireLockVersion(
            UUID tenantId, UUID siteId, UUID pageId, UUID sectionId, Integer lockVersion) {
        if (lockVersion == null) {
            throw badRequest("lockVersion 不能为空");
        }
        PageSectionState state = requireSection(tenantId, siteId, pageId, sectionId);
        if (!lockVersion.equals(state.getLockVersion())) {
            throw conflict("页面区块已被其他操作更新，请刷新后重试");
        }
        return state;
    }

    private void touch(PageSectionState state, int expectedVersion) {
        CmsActor actor = actorProvider.requireActor();
        state.setUpdatedAt(OffsetDateTime.now());
        state.setUpdatedBy(actor.userId());
        state.setUpdatedByType(actor.auditType());
        state.setLockVersion(expectedVersion + 1);
    }

    private void ensureUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw conflict("页面区块已被其他操作更新，请刷新后重试");
        }
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private CmsException sectionConflict() {
        return conflict("同一页面下的区块编码或锚点重复");
    }

    private CmsException conflict(String message) {
        return new CmsException(CmsErrorCode.CONFLICT, message);
    }

    private CmsException badRequest(String message) {
        return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
    }
}
