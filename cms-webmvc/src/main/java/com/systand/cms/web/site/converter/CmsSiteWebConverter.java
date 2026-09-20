package com.systand.cms.web.site.converter;

import com.systand.cms.api.site.CmsCreateSiteCommand;
import com.systand.cms.web.site.request.CreateSiteRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsSiteWebConverter {
    CmsCreateSiteCommand toCommand(CreateSiteRequest source);
}
