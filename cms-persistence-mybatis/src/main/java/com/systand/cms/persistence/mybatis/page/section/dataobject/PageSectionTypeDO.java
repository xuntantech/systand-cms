package com.systand.cms.persistence.mybatis.page.section.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 平台维护的 CMS 页面区块类型目录。 */
@Data
@TableName(value = "page_section_type", schema = "cms")
public class PageSectionTypeDO {
	@TableId(type = IdType.AUTO)
	private UUID id;
	private String code;
	private String scope;
	private UUID ownerTenantId;
	private String label;
	private String description;
	private String icon;
	private String defaultSectionKey;
	private String defaultAnchor;
	private String rendererKey;
	private String handlerKey;
	private Integer currentSchemaVersion;
	private String status;
	private Integer sortOrder;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	@TableLogic(value = "NULL", delval = "CURRENT_TIMESTAMP")
	private OffsetDateTime deletedAt;
	private UUID createdBy;
	private UUID updatedBy;
}
