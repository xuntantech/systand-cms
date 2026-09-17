package com.systand.cms.application.page.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.media.CmsMediaPort;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.application.page.dto.CreatePageRequest;
import com.systand.cms.application.page.dto.PublishPageRequest;
import com.systand.cms.application.page.dto.SchedulePageRequest;
import com.systand.cms.application.page.dto.UpdatePageRequest;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.application.page.section.service.PageSectionTypeDefinition;
import com.systand.cms.application.page.section.service.PageSectionTypeService;
import com.systand.cms.application.page.vo.PageVO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import com.systand.cms.core.error.CmsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
class PageServiceImplTests {
	@Mock
	private PageMapper pageMapper;
	@Mock
	private PageLocaleMapper pageLocaleMapper;
	@Mock
	private PageSectionMapper pageSectionMapper;
	@Mock
	private PageSectionLocaleMapper pageSectionLocaleMapper;
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

	private PageServiceImpl pageService;

	@BeforeEach
	void setUp() {
		pageService = new PageServiceImpl(
				pageMapper,
				pageLocaleMapper,
				pageSectionMapper,
				pageSectionLocaleMapper,
				siteMapper,
				actorProvider,
				mediaPort,
				tenantProvider,
				pageSectionTypeService);
	}

	@Test
	void createPageAlwaysStartsAsDraft() {
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(siteMapper.exists(any(Wrapper.class))).thenReturn(true);
		when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(userId));
		when(pageMapper.insert(any(PageDO.class))).thenAnswer(invocation -> {
			PageDO page = invocation.getArgument(0);
			page.setId(pageId);
			return 1;
		});
		when(pageMapper.selectOne(any(Wrapper.class))).thenAnswer(invocation -> {
			PageDO page = page(siteId, pageId, PagePublicationStatus.DRAFT, 1);
			page.setCreatedBy(userId);
			page.setUpdatedBy(userId);
			return page;
		});

		CreatePageRequest request = new CreatePageRequest();
		request.setCode("about");
		request.setRoutePath("/about");
		request.setLayoutSettings(Map.of("width", "wide"));

		PageVO result = pageService.createPage(siteId, request);

		assertEquals(pageId, result.getId());
		assertEquals(PagePublicationStatus.DRAFT, result.getPublicationStatus());
		assertEquals(1, result.getLockVersion());
	}

	@Test
	void updateRejectsStaleVersion() {
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		when(pageMapper.selectOne(any(Wrapper.class)))
				.thenReturn(page(siteId, pageId, PagePublicationStatus.DRAFT, 2));

		UpdatePageRequest request = new UpdatePageRequest();
		request.setCode("about");
		request.setRoutePath("/about");
		request.setPageKind(PageKind.STANDARD);
		request.setTemplateCode("default");
		request.setIsHome(false);
		request.setSortOrder(0);
		request.setLayoutSettings(Map.of());
		request.setLockVersion(1);

		CmsException exception = assertThrows(CmsException.class,
				() -> pageService.updatePage(siteId, pageId, request));

		assertEquals(409, exception.errorCode().httpStatus());
		verify(pageMapper, never()).update(any(), any(Wrapper.class));
	}

	@Test
	void scheduleRejectsInvalidPublicationWindow() {
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		PageDO draft = page(siteId, pageId, PagePublicationStatus.DRAFT, 1);
		when(pageMapper.selectOne(any(Wrapper.class))).thenReturn(draft);

		SchedulePageRequest request = new SchedulePageRequest();
		request.setLockVersion(1);
		request.setScheduledPublishAt(OffsetDateTime.now().plusDays(1));
		request.setScheduledUnpublishAt(OffsetDateTime.now());

		CmsException exception = assertThrows(CmsException.class,
				() -> pageService.schedulePage(siteId, pageId, request));

		assertEquals(400, exception.errorCode().httpStatus());
		verify(pageMapper, never()).update(any(), any(Wrapper.class));
	}

	@Test
	void publishRejectsHeroWithoutBackgroundMedia() {
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		PageDO draft = page(siteId, pageId, PagePublicationStatus.DRAFT, 1);
		when(pageMapper.selectOne(any(Wrapper.class))).thenReturn(draft);
		PageSectionDO hero = new PageSectionDO();
		hero.setId(UUID.randomUUID());
		hero.setSiteId(siteId);
		hero.setPageId(pageId);
		hero.setSectionKey("hero");
		hero.setSectionType("hero");
		hero.setSchemaVersion(1);
		hero.setIsVisible(true);
		when(pageSectionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(hero));
		when(pageSectionTypeService.requireAvailableDefinition("hero", 1))
				.thenReturn(heroDefinition());
		UUID tenantId = UUID.randomUUID();
		when(tenantProvider.requireTenantId()).thenReturn(tenantId);
		when(mediaPort.findBindings(
				tenantId, "PAGE_SECTION", hero.getId(), null)).thenReturn(List.of());
		PublishPageRequest request = new PublishPageRequest();
		request.setLockVersion(1);

		CmsException exception = assertThrows(CmsException.class,
				() -> pageService.publishPage(siteId, pageId, request));

		assertEquals(400, exception.errorCode().httpStatus());
		verify(pageMapper, never()).update(any(), any(Wrapper.class));
	}

	private PageSectionTypeDefinition heroDefinition() {
		return new PageSectionTypeDefinition(
				"hero", "BUILT_IN", "顶部横幅", "横幅", "icon",
				"hero", "", "hero", "HERO", 1, 10, Map.of(),
				List.of(
						new PageSectionTypeDefinition.MediaSlot(
								"BACKGROUND_IMAGE", "背景图片", List.of("image/*"), 1),
						new PageSectionTypeDefinition.MediaSlot(
								"BACKGROUND_VIDEO", "背景视频", List.of("video/*"), 1)),
				List.of("BACKGROUND_IMAGE", "BACKGROUND_VIDEO"));
	}

	private PageDO page(UUID siteId, UUID pageId, PagePublicationStatus status, int lockVersion) {
		PageDO page = new PageDO();
		page.setId(pageId);
		page.setSiteId(siteId);
		page.setCode("about");
		page.setRoutePath("/about");
		page.setPageKind(PageKind.STANDARD);
		page.setTemplateCode("default");
		page.setPublicationStatus(status);
		page.setIsHome(false);
		page.setSortOrder(0);
		page.setLayoutSettings(Map.of());
		page.setLockVersion(lockVersion);
		return page;
	}
}
