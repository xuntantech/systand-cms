package com.systand.cms.persistence.mybatis.site.mapper;

import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.persistence.mybatis.CmsBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Tenant-scoped site persistence.
 *
 * <p>This mapper is infrastructure-internal. Tenant-aware application code uses
 * {@code CmsSiteRepository}, whose implementation adds an explicit tenant
 * predicate to every operation. Cross-tenant administration belongs to
 * {@link SiteAdministrationMapper} and must never be added here by overriding
 * a base CRUD method.</p>
 */
@Mapper
public interface SiteMapper extends CmsBaseMapper<SiteDO> {
}
