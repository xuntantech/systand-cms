package com.systand.cms.application.page.section.vo;

import com.systand.cms.application.page.section.service.PageSectionTypeDefinition;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

/** 页面区块编辑器使用的类型定义。 */
@Value
@Builder
public class PageSectionTypeVO {
	String type;
	String scope;
	Integer schemaVersion;
	String label;
	String description;
	String icon;
	String defaultKey;
	String defaultAnchor;
	String rendererKey;
	Map<String, Object> defaultSettings;
	List<MediaSlotVO> mediaSlots;
	List<String> requiredAnyOfMediaRoles;

	public static PageSectionTypeVO from(PageSectionTypeDefinition type) {
		return PageSectionTypeVO.builder()
				.type(type.code())
				.scope(type.scope())
				.schemaVersion(type.schemaVersion())
				.label(type.label())
				.description(type.description())
				.icon(type.icon())
				.defaultKey(type.defaultKey())
				.defaultAnchor(type.defaultAnchor())
				.rendererKey(type.rendererKey())
				.defaultSettings(type.defaultSettings())
				.mediaSlots(type.mediaSlots().stream().map(MediaSlotVO::from).toList())
				.requiredAnyOfMediaRoles(type.requiredAnyOfMediaRoles())
				.build();
	}

	@Value
	@Builder
	public static class MediaSlotVO {
		String role;
		String label;
		List<String> acceptedMimeTypes;
		Integer maxCount;

		private static MediaSlotVO from(PageSectionTypeDefinition.MediaSlot slot) {
			return MediaSlotVO.builder()
					.role(slot.role())
					.label(slot.label())
					.acceptedMimeTypes(slot.acceptedMimeTypes())
					.maxCount(slot.maxCount())
					.build();
		}
	}
}
