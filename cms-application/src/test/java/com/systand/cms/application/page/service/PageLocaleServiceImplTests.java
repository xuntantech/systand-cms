package com.systand.cms.application.page.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.systand.cms.api.actor.CmsActor;
import com.systand.cms.api.actor.CmsActorProvider;
import com.systand.cms.api.tenant.CmsTenantProvider;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import com.systand.cms.application.page.dto.CreatePageLocaleRequest;
import com.systand.cms.application.page.dto.UpdatePageLocaleRequest;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.application.page.vo.PageLocaleVO;
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
class PageLocaleServiceImplTests {
	@Mock
	private PageLocaleMapper pageLocaleMapper;
	@Mock
	private PageMapper pageMapper;
	@Mock
	private SiteMapper siteMapper;
	@Mock
	private CmsActorProvider actorProvider;
	@Mock
	private CmsTenantProvider tenantProvider;

	private PageLocaleServiceImpl pageLocaleService;

	@BeforeEach
	void setUp() {
		pageLocaleService = new PageLocaleServiceImpl(
				pageLocaleMapper, pageMapper, siteMapper, actorProvider, tenantProvider);
	}

	@Test
	void createUsesParentScopeAndCurrentActor() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID localeId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		stubPageContext(tenantId, siteId, pageId, "zh-CN");
		when(actorProvider.requireActor()).thenReturn(CmsActor.tenantUser(userId));
		when(pageLocaleMapper.insert(any(PageLocaleDO.class))).thenAnswer(invocation -> {
			PageLocaleDO entity = invocation.getArgument(0);
			entity.setId(localeId);
			return 1;
		});
		when(pageLocaleMapper.selectOne(any(Wrapper.class)))
				.thenAnswer(invocation -> insertedLocale(localeId, tenantId, siteId, pageId, userId));

		CreatePageLocaleRequest request = new CreatePageLocaleRequest();
		request.setLocale("zh-cn");
		request.setTitle(" 关于我们 ");
		request.setMetadata(Map.of("ogType", "website"));

		PageLocaleVO result = pageLocaleService.createPageLocale(siteId, pageId, request);

		ArgumentCaptor<PageLocaleDO> captor = ArgumentCaptor.forClass(PageLocaleDO.class);
		verify(pageLocaleMapper).insert(captor.capture());
		PageLocaleDO inserted = captor.getValue();
		assertEquals(tenantId, inserted.getTenantId());
		assertEquals(siteId, inserted.getSiteId());
		assertEquals(pageId, inserted.getPageId());
		assertEquals("zh-CN", inserted.getLocale());
		assertEquals("关于我们", inserted.getTitle());
		assertEquals("tenant_user", inserted.getCreatedByType());
		assertEquals(localeId, result.getId());
	}

	@Test
	void updateRejectsStaleLocaleVersion() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID localeId = UUID.randomUUID();
		stubPageContext(tenantId, siteId, pageId, "zh-CN");
		PageLocaleDO current = insertedLocale(
				localeId, tenantId, siteId, pageId, UUID.randomUUID());
		current.setLockVersion(2);
		when(pageLocaleMapper.selectOne(any(Wrapper.class))).thenReturn(current);

		UpdatePageLocaleRequest request = new UpdatePageLocaleRequest();
		request.setTitle("关于我们");
		request.setMetadata(Map.of());
		request.setLockVersion(1);

		CmsException exception = assertThrows(CmsException.class,
				() -> pageLocaleService.updatePageLocale(siteId, pageId, localeId, request));

		assertEquals(409, exception.errorCode().httpStatus());
		verify(pageLocaleMapper, never()).update(any(), any(Wrapper.class));
	}

	@Test
	void deleteRejectsSiteDefaultLocale() {
		UUID tenantId = UUID.randomUUID();
		UUID siteId = UUID.randomUUID();
		UUID pageId = UUID.randomUUID();
		UUID localeId = UUID.randomUUID();
		stubPageContext(tenantId, siteId, pageId, "zh-CN");
		PageLocaleDO current = insertedLocale(
				localeId, tenantId, siteId, pageId, UUID.randomUUID());
		when(pageLocaleMapper.selectOne(any(Wrapper.class))).thenReturn(current);

		CmsException exception = assertThrows(CmsException.class,
				() -> pageLocaleService.deletePageLocale(siteId, pageId, localeId, 1));

		assertEquals(400, exception.errorCode().httpStatus());
		verify(pageLocaleMapper, never()).update(any(), any(Wrapper.class));
	}

	private void stubPageContext(UUID tenantId, UUID siteId, UUID pageId, String defaultLocale) {
		when(tenantProvider.requireTenantId()).thenReturn(tenantId);
		SiteDO site = new SiteDO();
		site.setId(siteId);
		site.setDefaultLocale(defaultLocale);
		site.setEnabledLocales(List.of("zh-CN", "en"));
		when(siteMapper.selectOne(any(Wrapper.class))).thenReturn(site);

		PageDO page = new PageDO();
		page.setId(pageId);
		page.setTenantId(tenantId);
		page.setSiteId(siteId);
		page.setPublicationStatus(PagePublicationStatus.DRAFT);
		when(pageMapper.selectOne(any(Wrapper.class))).thenReturn(page);
	}

	private PageLocaleDO insertedLocale(
			UUID localeId,
			UUID tenantId,
			UUID siteId,
			UUID pageId,
			UUID userId) {
		PageLocaleDO entity = new PageLocaleDO();
		entity.setId(localeId);
		entity.setTenantId(tenantId);
		entity.setSiteId(siteId);
		entity.setPageId(pageId);
		entity.setLocale("zh-CN");
		entity.setTitle("关于我们");
		entity.setMetadata(Map.of());
		entity.setLockVersion(1);
		entity.setCreatedBy(userId);
		entity.setCreatedByType("tenant_user");
		entity.setUpdatedBy(userId);
		entity.setUpdatedByType("tenant_user");
		return entity;
	}
}
