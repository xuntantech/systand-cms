package com.systand.cms.persistence.mybatis.page.converter;

import com.systand.cms.application.page.model.PageLocaleState;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageLocalePersistenceConverter {
    PageLocaleState toState(PageLocaleDO source);
    PageLocaleDO toDO(PageLocaleState source);
    List<PageLocaleState> toStates(List<PageLocaleDO> sources);
}
