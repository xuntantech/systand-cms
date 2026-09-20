package com.systand.cms.application.page.service;

import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.api.page.CmsPageLocaleService;
import com.systand.cms.api.page.CreatePageLocaleCommand;
import com.systand.cms.api.page.PageLocaleVO;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.UpdatePageLocaleCommand;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.application.page.converter.CmsPageLocaleConverter;
import com.systand.cms.application.page.model.PageLocaleState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.application.page.port.CmsPageLocaleRepository;
import com.systand.cms.application.page.port.CmsPageRepository;
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
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultCmsPageLocaleService implements CmsPageLocaleService {
    private final CmsPageLocaleRepository localeRepository;
    private final CmsPageRepository pageRepository;
    private final CmsSiteRepository siteRepository;
    private final CmsActorProvider actorProvider;
    private final CmsTenantProvider tenantProvider;
    private final CmsPageLocaleConverter converter;

    @Override
    public List<PageLocaleVO> getPageLocales(UUID siteId, UUID pageId) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId);
        return converter.toVOs(localeRepository.findAll(tenantId, siteId, pageId));
    }

    @Override
    public PageLocaleVO getPageLocale(UUID siteId, UUID pageId, UUID localeId) {
        UUID tenantId = tenantProvider.requireTenantId();
        requirePageContext(tenantId, siteId, pageId);
        return converter.toVO(requirePageLocale(tenantId, siteId, pageId, localeId));
    }

    @Override
    @Transactional
    public PageLocaleVO createPageLocale(UUID siteId, UUID pageId, CreatePageLocaleCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        PageContext context = requireEditablePageContext(tenantId, siteId, pageId);
        String locale = requireEnabledLocale(context.site(), command.locale());
        CmsActor actor = actorProvider.requireActor();
        PageLocaleState state = new PageLocaleState();
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setPageId(pageId);
        state.setLocale(locale);
        state.setTitle(command.title().trim());
        state.setSummary(trimToNull(command.summary()));
        state.setSeoTitle(trimToNull(command.seoTitle()));
        state.setSeoDescription(trimToNull(command.seoDescription()));
        state.setMetadata(new HashMap<>(command.metadata()));
        state.setLockVersion(1);
        state.setCreatedBy(actor.userId());
        state.setCreatedByType(actor.auditType());
        state.setUpdatedBy(actor.userId());
        state.setUpdatedByType(actor.auditType());
        try {
            return converter.toVO(localeRepository.insert(state));
        } catch (DuplicateKeyException exception) {
            throw conflict("该页面已存在 " + locale + " 语言版本");
        }
    }

    @Override
    @Transactional
    public PageLocaleVO updatePageLocale(
            UUID siteId, UUID pageId, UUID localeId, UpdatePageLocaleCommand command) {
        UUID tenantId = tenantProvider.requireTenantId();
        requireEditablePageContext(tenantId, siteId, pageId);
        PageLocaleState state = requireLockVersion(
                tenantId, siteId, pageId, localeId, command.lockVersion());
        int expectedVersion = state.getLockVersion();
        state.setTitle(command.title().trim());
        state.setSummary(trimToNull(command.summary()));
        state.setSeoTitle(trimToNull(command.seoTitle()));
        state.setSeoDescription(trimToNull(command.seoDescription()));
        state.setMetadata(new HashMap<>(command.metadata()));
        touch(state, expectedVersion);
        ensureUpdated(localeRepository.update(state, expectedVersion));
        return converter.toVO(requirePageLocale(tenantId, siteId, pageId, localeId));
    }

    @Override
    @Transactional
    public void deletePageLocale(UUID siteId, UUID pageId, UUID localeId, Integer lockVersion) {
        UUID tenantId = tenantProvider.requireTenantId();
        PageContext context = requireEditablePageContext(tenantId, siteId, pageId);
        PageLocaleState state = requireLockVersion(tenantId, siteId, pageId, localeId, lockVersion);
        if (StringUtils.hasText(context.site().getDefaultLocale())
                && context.site().getDefaultLocale().equalsIgnoreCase(state.getLocale())) {
            throw badRequest("默认语言不能删除，请先更换站点默认语言");
        }
        state.setDeletedAt(OffsetDateTime.now());
        touch(state, lockVersion);
        ensureUpdated(localeRepository.update(state, lockVersion));
    }

    private PageContext requireEditablePageContext(UUID tenantId, UUID siteId, UUID pageId) {
        PageContext context = requirePageContext(tenantId, siteId, pageId);
        if (context.page().getPublicationStatus() == PagePublicationStatus.ARCHIVED) {
            throw badRequest("已归档页面不能编辑语言版本");
        }
        return context;
    }

    private PageContext requirePageContext(UUID tenantId, UUID siteId, UUID pageId) {
        CmsSiteState site = siteRepository.findById(tenantId, siteId)
                .filter(candidate -> "ACTIVE".equals(candidate.getStatus()))
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "站点不存在或未启用"));
        PageState page = pageRepository.findById(tenantId, siteId, pageId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面不存在"));
        return new PageContext(site, page);
    }

    private PageLocaleState requirePageLocale(UUID tenantId, UUID siteId, UUID pageId, UUID localeId) {
        return localeRepository.findById(tenantId, siteId, pageId, localeId)
                .orElseThrow(() -> new CmsException(CmsErrorCode.NOT_FOUND, "页面语言版本不存在"));
    }

    private PageLocaleState requireLockVersion(
            UUID tenantId, UUID siteId, UUID pageId, UUID localeId, Integer lockVersion) {
        if (lockVersion == null) {
            throw badRequest("lockVersion 不能为空");
        }
        PageLocaleState current = requirePageLocale(tenantId, siteId, pageId, localeId);
        if (!lockVersion.equals(current.getLockVersion())) {
            throw conflict("语言版本已被其他操作更新，请刷新后重试");
        }
        return current;
    }

    private void touch(PageLocaleState state, int expectedVersion) {
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void ensureUpdated(int affectedRows) {
        if (affectedRows != 1) {
            throw conflict("语言版本已被其他操作更新，请刷新后重试");
        }
    }

    private CmsException conflict(String message) {
        return new CmsException(CmsErrorCode.CONFLICT, message);
    }

    private CmsException badRequest(String message) {
        return new CmsException(CmsErrorCode.INVALID_ARGUMENT, message);
    }

    private record PageContext(CmsSiteState site, PageState page) {
    }
}
