package com.systand.cms.application.site.service;

import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.core.error.CmsException;

import java.util.List;
import java.util.UUID;

/** Builds a valid site state while keeping creation defaults out of converters. */
public class CmsSiteFactory {

    public CmsSiteState create(
            UUID tenantId,
            UUID actorId,
            String code,
            String name,
            String defaultLocale,
            List<String> enabledLocales,
            String status) {
        if (tenantId == null) {
            throw new CmsException(CmsErrorCode.INVALID_ARGUMENT, "创建站点必须指定租户");
        }
        if (actorId == null) {
            throw new CmsException(CmsErrorCode.INVALID_ARGUMENT,
                    "创建站点需要用户身份；当前数据库约束不允许系统或匿名操作者");
        }
        String resolvedLocale = defaultLocale == null || defaultLocale.isBlank()
                ? "zh-CN" : defaultLocale;
        List<String> resolvedLocales = enabledLocales == null || enabledLocales.isEmpty()
                ? List.of(resolvedLocale) : List.copyOf(enabledLocales);
        if (!resolvedLocales.contains(resolvedLocale)) {
            throw new CmsException(CmsErrorCode.INVALID_ARGUMENT, "默认语言必须包含在启用语言中");
        }

        CmsSiteState site = new CmsSiteState();
        site.setTenantId(tenantId);
        site.setCode(code);
        site.setName(name);
        site.setDefaultLocale(resolvedLocale);
        site.setEnabledLocales(resolvedLocales);
        site.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);
        site.setCreatedBy(actorId);
        site.setUpdatedBy(actorId);
        return site;
    }
}
