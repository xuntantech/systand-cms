package com.systand.cms.web.page.converter;

import com.systand.cms.api.page.CreatePageCommand;
import com.systand.cms.api.page.CreatePageLocaleCommand;
import com.systand.cms.api.page.CreatePageSectionCommand;
import com.systand.cms.api.page.CreatePageSectionLocaleCommand;
import com.systand.cms.api.page.PublishPageCommand;
import com.systand.cms.api.page.SchedulePageCommand;
import com.systand.cms.api.page.UpdatePageCommand;
import com.systand.cms.api.page.UpdatePageLocaleCommand;
import com.systand.cms.api.page.UpdatePageSectionCommand;
import com.systand.cms.api.page.UpdatePageSectionLocaleCommand;
import com.systand.cms.web.page.request.CreatePageLocaleRequest;
import com.systand.cms.web.page.request.CreatePageRequest;
import com.systand.cms.web.page.request.PublishPageRequest;
import com.systand.cms.web.page.request.SchedulePageRequest;
import com.systand.cms.web.page.request.UpdatePageLocaleRequest;
import com.systand.cms.web.page.request.UpdatePageRequest;
import com.systand.cms.web.page.section.request.CreatePageSectionLocaleRequest;
import com.systand.cms.web.page.section.request.CreatePageSectionRequest;
import com.systand.cms.web.page.section.request.UpdatePageSectionLocaleRequest;
import com.systand.cms.web.page.section.request.UpdatePageSectionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageWebConverter {
    @Mapping(target = "home", source = "isHome")
    CreatePageCommand toCommand(CreatePageRequest source);

    @Mapping(target = "home", source = "isHome")
    UpdatePageCommand toCommand(UpdatePageRequest source);

    SchedulePageCommand toCommand(SchedulePageRequest source);
    PublishPageCommand toCommand(PublishPageRequest source);
    CreatePageLocaleCommand toCommand(CreatePageLocaleRequest source);
    UpdatePageLocaleCommand toCommand(UpdatePageLocaleRequest source);

    @Mapping(target = "visible", source = "isVisible")
    CreatePageSectionCommand toCommand(CreatePageSectionRequest source);

    @Mapping(target = "visible", source = "isVisible")
    UpdatePageSectionCommand toCommand(UpdatePageSectionRequest source);

    CreatePageSectionLocaleCommand toCommand(CreatePageSectionLocaleRequest source);
    UpdatePageSectionLocaleCommand toCommand(UpdatePageSectionLocaleRequest source);
}
