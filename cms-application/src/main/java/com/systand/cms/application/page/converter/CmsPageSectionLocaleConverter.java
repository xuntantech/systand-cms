package com.systand.cms.application.page.converter;

import com.systand.cms.api.page.PageSectionLocaleVO;
import com.systand.cms.application.page.model.PageSectionLocaleState;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageSectionLocaleConverter {
    PageSectionLocaleVO toVO(PageSectionLocaleState source);
    List<PageSectionLocaleVO> toVOs(List<PageSectionLocaleState> sources);
}
