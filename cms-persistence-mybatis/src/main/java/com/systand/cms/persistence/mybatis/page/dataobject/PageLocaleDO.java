package com.systand.cms.persistence.mybatis.page.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
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

@Data
@TableName(value = "page_locale", schema = "cms", autoResultMap = true)
public class PageLocaleDO {
	@TableId(type = IdType.AUTO)
	private UUID id;

	@TableField(fill = FieldFill.INSERT)
	private UUID tenantId;
	private UUID siteId;
	private UUID pageId;
	private String locale;
	private String title;
	private String summary;
	private String seoTitle;
	private String seoDescription;

	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> metadata;

	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	@TableLogic(value = "NULL", delval = "CURRENT_TIMESTAMP")
	private OffsetDateTime deletedAt;

	private UUID createdBy;
	private String createdByType;
	private UUID updatedBy;
	private String updatedByType;
}
