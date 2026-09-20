package com.systand.cms.application.page.converter;

import com.systand.cms.api.page.PageSectionVO;
import com.systand.cms.application.page.model.PageSectionState;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageSectionConverter {
    PageSectionVO toVO(PageSectionState source);
    List<PageSectionVO> toVOs(List<PageSectionState> sources);
}
