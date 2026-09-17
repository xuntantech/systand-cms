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

/** 页面区块类型的版本化内容、编辑器和媒体契约。 */
@Data
@TableName(value = "page_section_type_version", schema = "cms", autoResultMap = true)
public class PageSectionTypeVersionDO {
	@TableId(type = IdType.AUTO)
	private UUID id;
	private UUID sectionTypeId;
	private String sectionTypeCode;
	private Integer schemaVersion;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> contentSchema;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> settingsSchema;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> editorSchema;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> mediaSchema;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> defaultSettings;
	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> migrationConfig;
	private String status;
	private OffsetDateTime publishedAt;
	private Integer lockVersion;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	@TableLogic(value = "NULL", delval = "CURRENT_TIMESTAMP")
	private OffsetDateTime deletedAt;
	private UUID createdBy;
	private UUID updatedBy;
}
