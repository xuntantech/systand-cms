package com.systand.cms.persistence.mybatis.site.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.systand.cms.persistence.mybatis.CmsBaseMapper;
import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

/**
 * Explicit cross-tenant persistence for trusted CMS administration use cases.
 *
 * <p>This mapper is intentionally separate from {@link SiteMapper}. Callers
 * must complete host-level administrator authorization before invoking it.</p>
 */
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface SiteAdministrationMapper extends CmsBaseMapper<SiteDO> {

    @Select("""
            select *
            from cms.site
            order by created_at desc, id
            """)
    @InterceptorIgnore(tenantLine = "true")
    List<SiteDO> selectAllSites();

    @Select("""
            select *
            from cms.site
            where id = #{siteId}
            """)
    @InterceptorIgnore(tenantLine = "true")
    SiteDO selectSiteById(@Param("siteId") UUID siteId);

    @Select("""
            select *
            from cms.site
            where tenant_id = #{tenantId}
            order by created_at desc, id
            """)
    @InterceptorIgnore(tenantLine = "true")
    List<SiteDO> selectSitesByTenantId(@Param("tenantId") UUID tenantId);
}
