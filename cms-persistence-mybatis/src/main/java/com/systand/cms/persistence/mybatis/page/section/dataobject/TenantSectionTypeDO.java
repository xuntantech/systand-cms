package com.systand.cms.persistence.mybatis.page.section.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.systand.cms.persistence.mybatis.CmsJsonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** 租户对页面区块类型的授权与启用状态。 */
@Data
@TableName(value = "tenant_section_type", schema = "cms", autoResultMap = true)
public class TenantSectionTypeDO {
	@TableId(type = IdType.AUTO)
	private UUID id;
	private UUID tenantId;
	private UUID sectionTypeId;
	private String sectionTypeCode;
	private String status;
	private Boolean tenantEnabled;
	private String source;
	private Integer sortOrder;
	private OffsetDateTime activatedAt;
	private OffsetDateTime expiresAt;
	private OffsetDateTime disabledAt;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> configuration;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	@TableLogic(value = "NULL", delval = "CURRENT_TIMESTAMP")
	private OffsetDateTime deletedAt;
	private UUID createdBy;
	private UUID updatedBy;
}
