package com.systand.cms.persistence.mybatis.page.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.persistence.mybatis.CmsJsonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@TableName(value = "page", schema = "cms", autoResultMap = true)
public class PageDO {
	@TableId(type = IdType.AUTO)
	private UUID id;

	@TableField(fill = FieldFill.INSERT)
	private UUID tenantId;
	private UUID siteId;
	private String code;
	private String routePath;
	private PageKind pageKind;
	private String templateCode;
	private PagePublicationStatus publicationStatus;

	@TableField("is_home")
	private Boolean isHome;

	@TableField(typeHandler = CmsJsonTypeHandler.class)
	private Map<String, Object> layoutSettings;

	private Integer sortOrder;
	private OffsetDateTime scheduledPublishAt;
	private OffsetDateTime scheduledUnpublishAt;
	private OffsetDateTime publishedAt;
	private OffsetDateTime firstPublishedAt;
	private OffsetDateTime unpublishedAt;
	private OffsetDateTime archivedAt;
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
