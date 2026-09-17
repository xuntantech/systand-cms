package com.systand.cms.application.page.section.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionTypeMapper;
import com.systand.cms.persistence.mybatis.CmsUuidTypeHandler;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PageSectionTypeMapperTests {

	@Test
	void parsesCatalogQueryAndInitializationStatements() {
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.getTypeHandlerRegistry().register(UUID.class, CmsUuidTypeHandler.class);
		configuration.addMapper(PageSectionTypeMapper.class);

		String namespace = PageSectionTypeMapper.class.getName();
		assertTrue(configuration.hasStatement(namespace + ".initializeBuiltInsForTenant"));
		assertTrue(configuration.hasStatement(namespace + ".selectAvailableForTenant"));
	}
}
