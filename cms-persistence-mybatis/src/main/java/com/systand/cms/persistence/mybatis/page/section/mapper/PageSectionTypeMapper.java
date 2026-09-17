package com.systand.cms.persistence.mybatis.page.section.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeCatalogDO;
import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeDO;
import com.systand.cms.persistence.mybatis.CmsBaseMapper;
import com.systand.cms.persistence.mybatis.CmsJsonTypeHandler;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PageSectionTypeMapper extends CmsBaseMapper<PageSectionTypeDO> {

	@Insert("""
			INSERT INTO cms.tenant_section_type (
			    tenant_id, section_type_id, section_type_code,
			    status, tenant_enabled, source, sort_order
			)
			SELECT #{tenantId}, type.id, type.code,
			       'ENABLED', true, 'BUILT_IN', type.sort_order
			  FROM cms.page_section_type type
			 WHERE type.scope = 'BUILT_IN'
			   AND type.status = 'ACTIVE'
			   AND type.deleted_at IS NULL
			ON CONFLICT (tenant_id, section_type_code) DO NOTHING
			""")
	@InterceptorIgnore(tenantLine = "true")
	int initializeBuiltInsForTenant(@Param("tenantId") UUID tenantId);

	@Select("""
			<script>
			SELECT type.id,
			       type.code,
			       type.scope,
			       type.label,
			       type.description,
			       type.icon,
			       type.default_section_key,
			       type.default_anchor,
			       type.renderer_key,
			       type.handler_key,
			       version.schema_version,
			       entitlement.sort_order,
			       version.default_settings,
			       version.media_schema,
			       entitlement.configuration AS tenant_configuration
			  FROM cms.tenant_section_type entitlement
			  JOIN cms.page_section_type type
			    ON type.id = entitlement.section_type_id
			   AND type.code = entitlement.section_type_code
			  JOIN cms.page_section_type_version version
			    ON version.section_type_id = type.id
			   AND version.section_type_code = type.code
			   <choose>
			     <when test="schemaVersion != null">
			       AND version.schema_version = #{schemaVersion}
			     </when>
			     <otherwise>
			       AND version.schema_version = type.current_schema_version
			     </otherwise>
			   </choose>
			 WHERE entitlement.tenant_id = #{tenantId}
			   <if test="code != null">
			     AND entitlement.section_type_code = #{code}
			   </if>
			   AND entitlement.status = 'ENABLED'
			   AND entitlement.tenant_enabled = true
			   AND entitlement.activated_at &lt;= CURRENT_TIMESTAMP
			   AND (entitlement.expires_at IS NULL OR entitlement.expires_at &gt; CURRENT_TIMESTAMP)
			   AND entitlement.deleted_at IS NULL
			   AND type.status = 'ACTIVE'
			   AND type.deleted_at IS NULL
			   AND version.status = 'ACTIVE'
			   AND version.deleted_at IS NULL
			 ORDER BY entitlement.sort_order, type.sort_order, type.code
			</script>
			""")
	@Results(id = "pageSectionTypeCatalog", value = {
			@Result(column = "id", property = "id"),
			@Result(column = "default_settings", property = "defaultSettings",
					typeHandler = CmsJsonTypeHandler.class),
			@Result(column = "media_schema", property = "mediaSchema",
					typeHandler = CmsJsonTypeHandler.class),
			@Result(column = "tenant_configuration", property = "tenantConfiguration",
					typeHandler = CmsJsonTypeHandler.class)
	})
	@InterceptorIgnore(tenantLine = "true")
	List<PageSectionTypeCatalogDO> selectAvailableForTenant(
			@Param("tenantId") UUID tenantId,
			@Param("code") String code,
			@Param("schemaVersion") Integer schemaVersion);
}
