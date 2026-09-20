package com.systand.cms.persistence.mybatis.page.section.converter;

import com.systand.cms.core.section.PageSectionTypeDefinition;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeCatalogDO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageSectionTypeDefinitionConverterTests {

	@Test
	void buildsMediaContractFromDatabaseJson() {
		PageSectionTypeCatalogDO source = new PageSectionTypeCatalogDO();
		source.setCode("hero");
		source.setScope("BUILT_IN");
		source.setLabel("顶部横幅");
		source.setDefaultSectionKey("hero");
		source.setRendererKey("hero");
		source.setHandlerKey("HERO");
		source.setSchemaVersion(1);
		source.setDefaultSettings(Map.of("variant", "page_hero"));
		source.setMediaSchema(Map.of(
				"slots", List.of(Map.of(
						"role", "BACKGROUND_IMAGE",
						"accept", List.of("image/*"),
						"maxCount", 1), Map.of(
						"role", "BACKGROUND_VIDEO",
						"accept", List.of("video/*"),
						"maxCount", 1)),
				"requiredAnyOf", List.of("BACKGROUND_IMAGE", "BACKGROUND_VIDEO")));

		PageSectionTypeDefinition definition = PageSectionTypeDefinitionConverter.from(source);

		PageSectionTypeDefinition.MediaSlot slot = definition
				.findMediaSlot("background_image").orElseThrow();
		assertEquals("背景图片", slot.label());
		assertTrue(slot.accepts("image/webp"));
		assertEquals(List.of("BACKGROUND_IMAGE", "BACKGROUND_VIDEO"),
				definition.requiredAnyOfMediaRoles());
	}
}
