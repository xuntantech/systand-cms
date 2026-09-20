package com.systand.cms.persistence.mybatis.repository;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.systand.cms.application.page.model.PageLocaleState;
import com.systand.cms.application.page.model.PageSectionLocaleState;
import com.systand.cms.application.page.model.PageSectionState;
import com.systand.cms.application.page.model.PageState;
import com.systand.cms.persistence.mybatis.CmsUuidTypeHandler;
import com.systand.cms.persistence.mybatis.page.converter.CmsPageLocalePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.converter.CmsPagePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.dataobject.PageDO;
import com.systand.cms.persistence.mybatis.page.dataobject.PageLocaleDO;
import com.systand.cms.persistence.mybatis.page.mapper.PageLocaleMapper;
import com.systand.cms.persistence.mybatis.page.mapper.PageMapper;
import com.systand.cms.persistence.mybatis.page.repository.MybatisCmsPageLocaleRepository;
import com.systand.cms.persistence.mybatis.page.repository.MybatisCmsPageRepository;
import com.systand.cms.persistence.mybatis.page.section.converter.CmsPageSectionLocalePersistenceConverter;
import com.systand.cms.persistence.mybatis.page.section.converter.CmsPageSectionPersistenceConverter;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionLocaleDO;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionLocaleMapper;
import com.systand.cms.persistence.mybatis.page.section.mapper.PageSectionMapper;
import com.systand.cms.persistence.mybatis.page.section.repository.MybatisCmsPageSectionLocaleRepository;
import com.systand.cms.persistence.mybatis.page.section.repository.MybatisCmsPageSectionRepository;
import com.systand.cms.persistence.mybatis.site.converter.CmsSitePersistenceConverter;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.site.mapper.SiteMapper;
import com.systand.cms.persistence.mybatis.site.repository.MybatisCmsSiteRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CmsTenantScopedRepositoryTests {

    @BeforeAll
    static void initializeTableMetadata() {
        initialize(PageDO.class);
        initialize(PageLocaleDO.class);
        initialize(PageSectionDO.class);
        initialize(PageSectionLocaleDO.class);
        initialize(SiteDO.class);
    }

    @Test
    void siteQueriesContainTenantPredicate() {
        SiteMapper mapper = mock(SiteMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        MybatisCmsSiteRepository repository = new MybatisCmsSiteRepository(
                mapper, mock(CmsSitePersistenceConverter.class));

        repository.findAllByTenantId(UUID.randomUUID());

        ArgumentCaptor<Wrapper<SiteDO>> query = wrapperCaptor();
        verify(mapper).selectList(query.capture());
        assertContainsTenant(query.getValue());
    }

    @Test
    void pageQueriesAndUpdatesContainTenantPredicate() {
        PageMapper mapper = mock(PageMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of());
        MybatisCmsPageRepository repository = new MybatisCmsPageRepository(
                mapper, mock(CmsPagePersistenceConverter.class));
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();

        repository.findAll(tenantId, siteId, null, null, null);
        ArgumentCaptor<Wrapper<PageDO>> query = wrapperCaptor();
        verify(mapper).selectList(query.capture());
        assertContainsTenant(query.getValue());

        PageState state = new PageState();
        state.setTenantId(tenantId);
        state.setSiteId(siteId);
        state.setId(UUID.randomUUID());
        state.setLockVersion(2);
        repository.update(state, 1);
        ArgumentCaptor<Wrapper<PageDO>> update = wrapperCaptor();
        verify(mapper).update(isNull(), update.capture());
        assertContainsTenant(update.getValue());
    }

    @Test
    void childResourceQueriesAndUpdatesContainTenantPredicate() {
        UUID tenantId = UUID.randomUUID();
        UUID siteId = UUID.randomUUID();
        UUID pageId = UUID.randomUUID();

        PageLocaleMapper pageLocaleMapper = mock(PageLocaleMapper.class);
        when(pageLocaleMapper.selectList(any())).thenReturn(List.of());
        MybatisCmsPageLocaleRepository pageLocales = new MybatisCmsPageLocaleRepository(
                pageLocaleMapper, mock(CmsPageLocalePersistenceConverter.class));
        pageLocales.findAll(tenantId, siteId, pageId);
        ArgumentCaptor<Wrapper<PageLocaleDO>> pageLocaleQuery = wrapperCaptor();
        verify(pageLocaleMapper).selectList(pageLocaleQuery.capture());
        assertContainsTenant(pageLocaleQuery.getValue());

        PageLocaleState pageLocale = new PageLocaleState();
        pageLocale.setTenantId(tenantId);
        pageLocale.setSiteId(siteId);
        pageLocale.setPageId(pageId);
        pageLocale.setId(UUID.randomUUID());
        pageLocale.setLockVersion(2);
        pageLocales.update(pageLocale, 1);
        ArgumentCaptor<Wrapper<PageLocaleDO>> pageLocaleUpdate = wrapperCaptor();
        verify(pageLocaleMapper).update(isNull(), pageLocaleUpdate.capture());
        assertContainsTenant(pageLocaleUpdate.getValue());

        PageSectionMapper sectionMapper = mock(PageSectionMapper.class);
        when(sectionMapper.selectList(any())).thenReturn(List.of());
        MybatisCmsPageSectionRepository sections = new MybatisCmsPageSectionRepository(
                sectionMapper, mock(CmsPageSectionPersistenceConverter.class));
        sections.findAll(tenantId, siteId, pageId, null, null);
        ArgumentCaptor<Wrapper<PageSectionDO>> sectionQuery = wrapperCaptor();
        verify(sectionMapper).selectList(sectionQuery.capture());
        assertContainsTenant(sectionQuery.getValue());

        PageSectionLocaleMapper sectionLocaleMapper = mock(PageSectionLocaleMapper.class);
        when(sectionLocaleMapper.selectList(any())).thenReturn(List.of());
        MybatisCmsPageSectionLocaleRepository sectionLocales =
                new MybatisCmsPageSectionLocaleRepository(
                        sectionLocaleMapper, mock(CmsPageSectionLocalePersistenceConverter.class));
        sectionLocales.findAll(tenantId, siteId, pageId, UUID.randomUUID());
        ArgumentCaptor<Wrapper<PageSectionLocaleDO>> sectionLocaleQuery = wrapperCaptor();
        verify(sectionLocaleMapper).selectList(sectionLocaleQuery.capture());
        assertContainsTenant(sectionLocaleQuery.getValue());
    }

    private static void initialize(Class<?> dataObjectType) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, CmsUuidTypeHandler.class);
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(configuration, ""), dataObjectType);
    }

    private static void assertContainsTenant(Wrapper<?> wrapper) {
        assertTrue(wrapper.getSqlSegment().contains("tenant_id"), wrapper.getSqlSegment());
    }

    private static <T> ArgumentCaptor<Wrapper<T>> wrapperCaptor() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Wrapper<T>> captor = ArgumentCaptor.forClass(Wrapper.class);
        return captor;
    }
}
