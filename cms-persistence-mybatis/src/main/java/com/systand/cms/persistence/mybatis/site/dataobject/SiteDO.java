package com.systand.cms.persistence.mybatis.site.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.systand.cms.persistence.mybatis.CmsJsonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@TableName(value = "site", schema = "cms", autoResultMap = true)
public class SiteDO {
	@TableId(type = IdType.AUTO)
	private UUID id;
	private UUID tenantId;
	private String code;
	private String name;
	private String defaultLocale;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private List<String> enabledLocales;
	private String status;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	private UUID createdBy;
	private UUID updatedBy;
}
