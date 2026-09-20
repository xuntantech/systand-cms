package com.systand.cms.application.page.converter;

import com.systand.cms.api.page.PageVO;
import com.systand.cms.application.page.model.PageState;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageConverter {
    PageVO toVO(PageState source);
    List<PageVO> toVOs(List<PageState> sources);
}
