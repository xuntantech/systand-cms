package com.systand.cms.persistence.mybatis.page.section.dataobject;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

/** 当前租户可用的区块类型联查结果。 */
@Data
public class PageSectionTypeCatalogDO {
	private UUID id;
	private String code;
	private String scope;
	private String label;
	private String description;
	private String icon;
	private String defaultSectionKey;
	private String defaultAnchor;
	private String rendererKey;
	private String handlerKey;
	private Integer schemaVersion;
	private Integer sortOrder;
	private Map<String, Object> defaultSettings;
	private Map<String, Object> mediaSchema;
	private Map<String, Object> tenantConfiguration;
}
