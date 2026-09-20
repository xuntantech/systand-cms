package com.systand.cms.persistence.mybatis.page.converter;

import com.systand.cms.application.page.model.PageState;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPagePersistenceConverter {
    @Mapping(target = "home", source = "isHome")
    PageState toState(PageDO source);

    @Mapping(target = "isHome", source = "home")
    PageDO toDO(PageState source);

    List<PageState> toStates(List<PageDO> sources);
}
