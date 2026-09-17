package com.systand.cms.application.page.section.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.application.page.section.dto.CreatePageSectionLocaleRequest;
import com.systand.cms.api.page.SectionTranslationStatus;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import com.systand.cms.core.error.CmsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageSectionLocaleServiceImplTests {
	@Mock
	private PageSectionLocaleMapper pageSectionLocaleMapper;
	@Mock
	private PageSectionMapper pageSectionMapper;
	@Mock
	private PageMapper pageMapper;
	@Mock
	private SiteMapper siteMapper;
	@Mock
	private CmsActorProvider actorProvider;
	@Mock
	private CmsTenantProvider tenantProvider;

	private PageSectionLocaleServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new PageSectionLocaleServiceImpl(
				pageSectionLocaleMapper, pageSectionMapper, pageMapper, siteMapper, actorProvider, tenantProvider);
	}

	@Test
	void createCanonicalizesLocaleAndUsesParentScope() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		UUID localeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		stubContext(tenantId, siteId, pageId, sectionId);
		when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(userId));
		when(pageSectionLocaleMapper.insert(any(PageSectionLocaleDO.class))).thenAnswer(invocation -> {
			PageSectionLocaleDO entity = invocation.getArgument(0);
			entity.setId(localeId);
			return 1;
		});
		when(pageSectionLocaleMapper.selectOne(any(Wrapper.class))).thenAnswer(invocation ->
				locale(localeId, tenantId, siteId, pageId, sectionId, userId));

		CreatePageSectionLocaleRequest request = new CreatePageSectionLocaleRequest();
		request.setLocale("zh-cn");
		request.setTranslationStatus(SectionTranslationStatus.COMPLETE);
		request.setContent(Map.of("title", "走进和济"));
		service.createSectionLocale(siteId, pageId, sectionId, request);

		ArgumentCaptor<PageSectionLocaleDO> captor =
				ArgumentCaptor.forClass(PageSectionLocaleDO.class);
		verify(pageSectionLocaleMapper).insert(captor.capture());
		PageSectionLocaleDO inserted = captor.getValue();
		assertEquals(tenantId, inserted.getTenantId());
		assertEquals("zh-CN", inserted.getLocale());
		assertEquals("tenant_user", inserted.getCreatedByType());
	}

	@Test
	void completeContentCannotBeEmpty() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		stubContext(tenantId, siteId, pageId, sectionId);

		CreatePageSectionLocaleRequest request = new CreatePageSectionLocaleRequest();
		request.setLocale("zh-CN");
		request.setTranslationStatus(SectionTranslationStatus.COMPLETE);
		request.setContent(Map.of());

		CmsException exception = assertThrows(CmsException.class,
				() -> service.createSectionLocale(siteId, pageId, sectionId, request));
		assertEquals(400, exception.errorCode().httpStatus());
		verify(pageSectionLocaleMapper, never()).insert(any(PageSectionLocaleDO.class));
	}

	private void stubContext(UUID tenantId, UUID siteId, UUID pageId, UUID sectionId) {
		when(tenantProvider.requireTenantId()).thenReturn(tenantId);
		SiteDO site = new SiteDO();
		site.setId(siteId);
		site.setDefaultLocale("zh-CN");
		site.setEnabledLocales(List.of("zh-CN", "en"));
		when(siteMapper.selectOne(any(Wrapper.class))).thenReturn(site);

		PageDO page = new PageDO();
		page.setId(pageId);
		page.setTenantId(tenantId);
		page.setSiteId(siteId);
		page.setPublicationStatus(PagePublicationStatus.DRAFT);
		when(pageMapper.selectOne(any(Wrapper.class))).thenReturn(page);

		PageSectionDO section = new PageSectionDO();
		section.setId(sectionId);
		section.setTenantId(tenantId);
		section.setSiteId(siteId);
		section.setPageId(pageId);
		section.setSectionType("hero");
		section.setSchemaVersion(1);
		when(pageSectionMapper.selectOne(any(Wrapper.class))).thenReturn(section);
	}

	private PageSectionLocaleDO locale(
			UUID localeId,
			UUID tenantId,
			UUID siteId,
			UUID pageId,
			UUID sectionId,
			UUID userId) {
		PageSectionLocaleDO entity = new PageSectionLocaleDO();
		entity.setId(localeId);
		entity.setTenantId(tenantId);
		entity.setSiteId(siteId);
		entity.setPageId(pageId);
		entity.setSectionId(sectionId);
		entity.setLocale("zh-CN");
		entity.setTranslationStatus(SectionTranslationStatus.COMPLETE);
		entity.setContent(Map.of("title", "走进和济"));
		entity.setLockVersion(1);
		entity.setCreatedBy(userId);
		entity.setUpdatedBy(userId);
		return entity;
	}
}
