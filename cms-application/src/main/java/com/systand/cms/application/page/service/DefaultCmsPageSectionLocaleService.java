package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.page.CmsPageSectionLocaleService;
import com.systand.cms.api.page.CreatePageSectionLocaleCommand;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.PageSectionLocaleVO;
import com.systand.cms.api.page.SectionTranslationStatus;
import com.systand.cms.api.page.UpdatePageSectionLocaleCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageSectionLocaleConverter;
import com.systand.cms.application.page.model.PageSectionLocaleState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageRepository;
import com.systand.cms.application.page.port.CmsPageSectionLocaleRepository;
import com.systand.cms.application.page.port.CmsPageSectionRepository;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.application.site.port.CmsSiteRepository;
import com.systand.cms.core.error.CmsException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsPageSectionLocaleService implements CmsPageSectionLocaleService {
    private final CmsPageSectionLocaleRepository localeRepository;
    private final CmsPageSectionRepository sectionRepository;
    private final CmsPageRepository pageRepository;
    private final CmsSiteRepository siteRepository;
    private final CmsActorProvider actorProvider;
    private final CmsTenantProvider tenantProvider;
    private final CmsPageSectionLocaleConverter converter;

    @Override
    public List<PageSectionLocaleVO> getSectionLocales(UUID siteId, UUID pageId, UUID sectionId) {
        UUID tenantId = tenantProvider.requireTenantId();
        requireContext(tenantId, siteId, pageId, sectionId, false);
        return converter.toVOs(localeRepository.findAll(tenantId, siteId, pageId, sectionId));
    }

    @Override
    public PageSectionLocaleVO getSectionLocale(
            UUID siteId, UUID pageId, UUID sectionId, UUID localeId) {
        UUID tenantId = tenantProvider.requireTenantId();
        requireContext(tenantId, siteId, pageId, sectionId, false);
        return converter.toVO(requireLocale(tenantId, siteId, pageId, sectionId, localeId));
    }

    @Override
    @Transactional
    public PageSectionLocaleVO createSectionLocale(
            UUID siteId, UUID pageId, UUID sectionId, CreatePageSectionLocaleCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        SectionContext context = requireContext(tenantId, siteId, pageId, sectionId, true);
        String locale = requireEnabledLocale(context.site(), command.locale());
        validateContentState(command.translationStatus(), command.content());
        CmsActor actor = actorProvider.requireActor();
        PageSectionLocaleState state = new PageSectionLocaleState();
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setPageId(pageId);
        state.setSectionId(sectionId);
        state.setLocale(locale);
        state.setTranslationStatus(command.translationStatus());
        state.setContent(new HashMap<>(command.content()));
        state.setLockVersion(1);
        state.setCreatedBy(actor.userId());
        state.setCreatedByType(actor.auditType());
        state.setUpdatedBy(actor.userId());
        state.setUpdatedByType(actor.auditType());
        try {
            return converter.toVO(localeRepository.insert(state));
        } catch (DuplicateKeyException exception) {
            throw conflict("该页面区块已存在 " + locale + " 语言内容");
        }
    }

    @Override
    @Transactional
    public PageSectionLocaleVO updateSectionLocale(
            UUID siteId, UUID pageId, UUID sectionId, UUID localeId,
            UpdatePageSectionLocaleCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        requireContext(tenantId, siteId, pageId, sectionId, true);
        PageSectionLocaleState state = requireLockVersion(
                tenantId, siteId, pageId, sectionId, localeId, command.lockVersion());
        validateContentState(command.translationStatus(), command.content());
        int expectedVersion = state.getLockVersion();
        state.setTranslationStatus(command.translationStatus());
        state.setContent(new HashMap<>(command.content()));
        touch(state, expectedVersion);
        ensureUpdated(localeRepository.update(state, expectedVersion));
        return converter.toVO(requireLocale(tenantId, siteId, pageId, sectionId, localeId));
    }

    @Override
    @Transactional
    public void deleteSectionLocale(
            UUID siteId, UUID pageId, UUID sectionId, UUID localeId, Integer lockVersion) {
        UUID tenantId = tenantProvider.requireTenantId();
        requireContext(tenantId, siteId, pageId, sectionId, true);
        PageSectionLocaleState state = requireLockVersion(
                tenantId, siteId, pageId, sectionId, localeId, lockVersion);
        state.setDeletedAt(OffsetDateTime.now());
        touch(state, lockVersion);
        ensureUpdated(localeRepository.update(state, lockVersion));
    }

    private SectionContext requireContext(
            UUID tenantId, UUID siteId, UUID pageId, UUID sectionId, boolean editable) {
        CmsSiteState site = siteRepository.findById(tenantId, siteId)
                .filter(candidate -> "ACTIVE".equals(candidate.getStatus()))
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用"));
        PageState page = pageRepository.findById(tenantId, siteId, pageId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在"));
        if (editable && page.getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
            throw badRequest("已归档页面不能编辑区块语言内容");
        }
        if (sectionRepository.findById(tenantId, siteId, pageId, sectionId).isEmpty()) {
            throw new CmsException(CmsErrorCode.NOT_FOUND, "页面区块不存在");
        }
        return new SectionContext(site, page);
    }

    private PageSectionLocaleState requireLocale(
            UUID tenantId, UUID siteId, UUID pageId, UUID sectionId, UUID localeId) {
        return localeRepository.findById(tenantId, siteId, pageId, sectionId, localeId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面区块语言内容不存在"));
    }

    private PageSectionLocaleState requireLockVersion(
            UUID tenantId, UUID siteId, UUID pageId, UUID sectionId,
            UUID localeId, Integer lockVersion) {
        if (lockVersion == null) {
            throw badRequest("lockVersion 不能为空");
        }
        PageSectionLocaleState state = requireLocale(
                tenantId, siteId, pageId, sectionId, localeId);
        if (!lockVersion.equals(state.getLockVersion())) {
            throw conflict("页面区块语言内容已被其他操作更新，请刷新后重试");
        }
        return state;
    }

    private void touch(PageSectionLocaleState state, int expectedVersion) {
        CmsActor actor = actorProvider.requireActor();
        state.setUpdatedAt(OffsetDateTime.now());
        state.setUpdatedBy(actor.userId());
        state.setUpdatedByType(actor.auditType());
        state.setLockVersion(expectedVersion + 1);
    }

    private String requireEnabledLocale(CmsSiteState site, String requestedLocale) {
        String locale = requestedLocale.trim();
        if (site.getEnabledLocales() != null && !site.getEnabledLocales().isEmpty()) {
            return site.getEnabledLocales().stream()
                    .filter(candidate -> candidate.equalsIgnoreCase(locale))
                    .findFirst()
                    .orElseThrow(() -> badRequest("站点未启用语言: " + locale));
        }
        if (StringUtils.hasText(site.getDefaultLocale())) {
            if (site.getDefaultLocale().equalsIgnoreCase(locale)) {
                return site.getDefaultLocale();
            }
            throw badRequest("站点未启用语言: " + locale);
        }
        return locale;
    }

    private void validateContentState(SectionTranslationStatus status, Map<String, Object> content) {
        if (status == null || content == null) {
            throw badRequest("translationStatus 和 content 不能为空");
        }
        if (status == SectionTranslationStatus.COMPLETE && content.isEmpty()) {
            throw badRequest("标记为 COMPLETE 的区块语言内容不能为空");
        }
    }

    private void ensureUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw conflict("页面区块语言内容已被其他操作更新，请刷新后重试");
        }
    }

    private CmsException conflict(String message) {
        return new CmsException(CmsErrorCode.CONFLICT, message);
    }

    private CmsException badRequest(String message) {
        return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
    }

    private record SectionContext(CmsSiteState site, PageState page) {
    }
}
