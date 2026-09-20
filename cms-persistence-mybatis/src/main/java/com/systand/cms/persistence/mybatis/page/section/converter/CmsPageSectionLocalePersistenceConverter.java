package com.systand.cms.persistence.mybatis.page.section.converter;

import com.systand.cms.application.page.model.PageSectionLocaleState;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageSectionLocalePersistenceConverter {
    PageSectionLocaleState toState(PageSectionLocaleDO source);
    PageSectionLocaleDO toDO(PageSectionLocaleState source);
    List<PageSectionLocaleState> toStates(List<PageSectionLocaleDO> sources);
}
