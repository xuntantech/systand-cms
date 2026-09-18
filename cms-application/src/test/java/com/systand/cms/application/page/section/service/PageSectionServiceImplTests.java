package com.systand.cms.application.page.section.service;

import com.systand.cms.core.section.PageSectionTypeDefinition;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.application.page.section.dto.CreatePageSectionRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionRequest;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import com.systand.cms.core.error.CmsException;
import com.systand.cms.api.error.CmsErrorCode;
import com.systand.cms.persistence.mybatis.CmsUuidTypeHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

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
class PageSectionServiceImplTests {
	@Mock
	private PageSectionMapper pageSectionMapper;
	@Mock
	private PageSectionLocaleMapper pageSectionLocaleMapper;
	@Mock
	private PageMapper pageMapper;
	@Mock
	private SiteMapper siteMapper;
	@Mock
	private CmsActorProvider actorProvider;
	@Mock
	private CmsMediaPort mediaPort;
	@Mock
	private CmsTenantProvider tenantProvider;
	@Mock
	private PageSectionTypeService pageSectionTypeService;

	private PageSectionServiceImpl service;

	@BeforeEach
	void setUp() {
		initializeTableInfo(PageSectionDO.class);
		initializeTableInfo(PageSectionLocaleDO.class);
		service = new PageSectionServiceImpl(
				pageSectionMapper, pageSectionLocaleMapper, pageMapper, siteMapper,
				actorProvider, mediaPort, tenantProvider, pageSectionTypeService);
	}

	private void initializeTableInfo(Class<?> entityType) {
		MybatisConfiguration configuration = new MybatisConfiguration();
		configuration.getTypeHandlerRegistry().register(UUID.class, CmsUuidTypeHandler.class);
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
		assistant.setCurrentNamespace(getClass().getName() + "." + entityType.getSimpleName());
		TableInfoHelper.initTableInfo(assistant, entityType);
	}

	@Test
	void createUsesPageScopeAndCurrentActor() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		stubPage(tenantId, siteId, pageId);
		when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(userId));
		when(pageSectionTypeService.requireAvailableDefinition("rich_text", 1))
				.thenReturn(richTextDefinition());
		when(pageSectionMapper.insert(any(PageSectionDO.class))).thenAnswer(invocation -> {
			PageSectionDO entity = invocation.getArgument(0);
			entity.setId(sectionId);
			return 1;
		});
		when(pageSectionMapper.selectOne(any(Wrapper.class))).thenAnswer(invocation ->
				section(sectionId, tenantId, siteId, pageId, userId, 1));

		CreatePageSectionRequest request = new CreatePageSectionRequest();
		request.setSectionKey("group_profile");
		request.setSectionType("rich_text");
		request.setAdminLabel(" 集团概况 ");
		request.setSettings(Map.of("theme", "light"));

		service.createSection(siteId, pageId, request);

		ArgumentCaptor<PageSectionDO> captor = ArgumentCaptor.forClass(PageSectionDO.class);
		verify(pageSectionMapper).insert(captor.capture());
		PageSectionDO inserted = captor.getValue();
		assertEquals(tenantId, inserted.getTenantId());
		assertEquals(siteId, inserted.getSiteId());
		assertEquals(pageId, inserted.getPageId());
		assertEquals("集团概况", inserted.getAdminLabel());
		assertEquals("tenant_user", inserted.getCreatedByType());
		assertEquals("two_column_intro", inserted.getSettings().get("variant"));
		assertEquals("wide", inserted.getSettings().get("contentWidth"));
		assertEquals("light", inserted.getSettings().get("theme"));
	}

	@Test
	void createRejectsUnknownSectionType() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		stubPage(tenantId, siteId, pageId);
		CreatePageSectionRequest request = new CreatePageSectionRequest();
		request.setSectionKey("unknown");
		request.setSectionType("unknown");
		when(pageSectionTypeService.requireAvailableDefinition("unknown", 1))
				.thenThrow(new CmsException(CmsErrorCode.INVALID_ARGUMENT, "不可用"));

		CmsException exception = assertThrows(CmsException.class,
				() -> service.createSection(siteId, pageId, request));

		assertEquals(400, exception.errorCode().httpStatus());
		verify(pageSectionMapper, never()).insert(any(PageSectionDO.class));
	}

	@Test
	void updateRejectsStaleVersion() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		stubPage(tenantId, siteId, pageId);
		when(pageSectionMapper.selectOne(any(Wrapper.class))).thenReturn(
				section(sectionId, tenantId, siteId, pageId, UUID.randomUUID(), 2));

		UpdatePageSectionRequest request = updateRequest(1);
		CmsException exception = assertThrows(CmsException.class,
				() -> service.updateSection(siteId, pageId, sectionId, request));

		assertEquals(409, exception.errorCode().httpStatus());
		verify(pageSectionMapper, never()).update(any(), any(Wrapper.class));
	}

	@Test
	void deleteSoftDeletesChildLocales() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID sectionId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		stubPage(tenantId, siteId, pageId);
		when(pageSectionMapper.selectOne(any(Wrapper.class))).thenReturn(
				section(sectionId, tenantId, siteId, pageId, userId, 1));
		when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(userId));
		when(pageSectionMapper.update(any(), any(Wrapper.class))).thenReturn(1);
		when(tenantProvider.requireTenantId()).thenReturn(tenantId);

		service.deleteSection(siteId, pageId, sectionId, 1);

		verify(pageSectionLocaleMapper).update(any(), any(Wrapper.class));
		verify(mediaPort).deleteResourceBindings(tenantId, "PAGE_SECTION", sectionId);
	}

	private void stubPage(UUID tenantId, UUID siteId, UUID pageId) {
		when(siteMapper.exists(any(Wrapper.class))).thenReturn(true);
		PageDO page = new PageDO();
		page.setId(pageId);
		page.setTenantId(tenantId);
		page.setSiteId(siteId);
		page.setPublicationStatus(PagePublicationStatus.DRAFT);
		when(pageMapper.selectOne(any(Wrapper.class))).thenReturn(page);
	}

	private UpdatePageSectionRequest updateRequest(int lockVersion) {
		UpdatePageSectionRequest request = new UpdatePageSectionRequest();
		request.setSectionKey("group_profile");
		request.setSectionType("rich_text");
		request.setSortOrder(10);
		request.setIsVisible(true);
		request.setSettings(Map.of());
		request.setSchemaVersion(1);
		request.setLockVersion(lockVersion);
		return request;
	}

	private PageSectionTypeDefinition richTextDefinition() {
		return new PageSectionTypeDefinition(
				"rich_text", "BUILT_IN", "图文介绍", "正文", "icon",
				"group_profile", "group-profile", "rich_text", "RICH_TEXT",
				1, 20, Map.of("variant", "two_column_intro", "contentWidth", "wide"),
				List.of(), List.of());
	}

	private PageSectionDO section(
			UUID sectionId,
			UUID tenantId,
			UUID siteId,
			UUID pageId,
			UUID userId,
			int lockVersion) {
		PageSectionDO entity = new PageSectionDO();
		entity.setId(sectionId);
		entity.setTenantId(tenantId);
		entity.setSiteId(siteId);
		entity.setPageId(pageId);
		entity.setSectionKey("group_profile");
		entity.setSectionType("rich_text");
		entity.setSortOrder(10);
		entity.setIsVisible(true);
		entity.setSettings(Map.of());
		entity.setSchemaVersion(1);
		entity.setLockVersion(lockVersion);
		entity.setCreatedBy(userId);
		entity.setUpdatedBy(userId);
		return entity;
	}
}
