package com.systand.cms.persistence.mybatis.site.converter;

import com.systand.cms.application.site.model.CmsSiteState;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsSitePersistenceConverter {
    CmsSiteState toState(SiteDO source);
    SiteDO toDO(CmsSiteState source);
    List<CmsSiteState> toStates(List<SiteDO> sources);
}
