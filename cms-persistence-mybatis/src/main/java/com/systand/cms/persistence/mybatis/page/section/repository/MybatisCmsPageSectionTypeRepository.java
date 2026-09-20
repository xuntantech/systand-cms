package com.systand.cms.persistence.mybatis.page.section.repository;

import com.systand.cms.application.page.port.CmsPageSectionTypeRepository;
import com.systand.cms.core.section.PageSectionTypeDefinition;
import com.systand.cms.persistence.mybatis.page.section.converter.PageSectionTypeDefinitionConverter;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MybatisCmsPageSectionTypeRepository implements CmsPageSectionTypeRepository {
    private final PageSectionTypeMapper mapper;

    @Override
    public void initializeBuiltIns(UUID tenantId) {
        mapper.initializeBuiltInsForTenant(tenantId);
    }

    @Override
    public List<PageSectionTypeDefinition> findAvailable(UUID tenantId, String code, Integer schemaVersion) {
        return mapper.selectAvailableForTenant(tenantId, code, schemaVersion).stream()
                .map(PageSectionTypeDefinitionConverter::from)
                .toList();
    }
}
