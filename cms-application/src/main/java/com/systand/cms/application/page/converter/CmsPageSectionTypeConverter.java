package com.systand.cms.application.page.converter;

import com.systand.cms.api.page.PageSectionTypeVO;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CmsPageSectionTypeConverter {
    @Mapping(target = "type", source = "code")
    @Mapping(target = "defaultKey", source = "defaultKey")
    PageSectionTypeVO toVO(PageSectionTypeDefinition source);

    @Mapping(target = "acceptedMimeTypes", source = "acceptedMimeTypes")
    PageSectionTypeVO.MediaSlotVO toVO(PageSectionTypeDefinition.MediaSlot source);

    List<PageSectionTypeVO> toVOs(List<PageSectionTypeDefinition> sources);
}
