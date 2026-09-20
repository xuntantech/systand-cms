package com.systand.cms.application.site.converter;

import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.application.site.model.CmsSiteState;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/** Generated mapping from CMS persistence state to its public site view. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsSiteConverter {
    CmsSiteVO toVO(CmsSiteState source);

    List<CmsSiteVO> toVOs(List<CmsSiteState> sources);
}
