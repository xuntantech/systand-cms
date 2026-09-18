package com.systand.cms.application.site.service;

import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** Generated mapping from CMS persistence state to its public site view. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsSiteVOMapper {
    CmsSiteVO toVO(SiteDO source);

    List<CmsSiteVO> toVOs(List<SiteDO> sources);
}
