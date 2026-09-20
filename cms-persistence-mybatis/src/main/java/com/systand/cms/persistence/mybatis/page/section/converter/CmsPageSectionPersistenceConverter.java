package com.systand.cms.persistence.mybatis.page.section.converter;

import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageSectionPersistenceConverter {
    @Mapping(target = "visible", source = "isVisible")
    PageSectionState toState(PageSectionDO source);

    @Mapping(target = "isVisible", source = "visible")
    PageSectionDO toDO(PageSectionState source);

    List<PageSectionState> toStates(List<PageSectionDO> sources);
}
