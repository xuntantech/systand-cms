package com.systand.cms.application.page.section.service;

import com.systand.cms.persistence.mybatis.page.section.dataobject.PageSectionTypeCatalogDO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** 已通过租户授权校验的区块类型运行时定义。 */
public record PageSectionTypeDefinition(
		String code,
		String scope,
		String label,
		String description,
		String icon,
		String defaultKey,
		String defaultAnchor,
		String rendererKey,
		String handlerKey,
		Integer schemaVersion,
		Integer sortOrder,
		Map<String, Object> defaultSettings,
		List<MediaSlot> mediaSlots,
		List<String> requiredAnyOfMediaRoles) {

	public static PageSectionTypeDefinition from(PageSectionTypeCatalogDO source) {
		Map<String, Object> mediaSchema = source.getMediaSchema() == null
				? Map.of() : source.getMediaSchema();
		return new PageSectionTypeDefinition(
				source.getCode(),
				source.getScope(),
				source.getLabel(),
				source.getDescription(),
				source.getIcon(),
				source.getDefaultSectionKey(),
				source.getDefaultAnchor(),
				source.getRendererKey(),
				source.getHandlerKey(),
				source.getSchemaVersion(),
				source.getSortOrder(),
				copyMap(source.getDefaultSettings()),
				parseMediaSlots(mediaSchema.get("slots")),
				parseStrings(mediaSchema.get("requiredAnyOf")));
	}

	public Map<String, Object> mergeSettings(Map<String, Object> settings) {
		Map<String, Object> merged = new LinkedHashMap<>(defaultSettings);
		if (settings != null) {
			merged.putAll(settings);
		}
		return merged;
	}

	public Optional<MediaSlot> findMediaSlot(String fileRole) {
		if (fileRole == null || fileRole.isBlank()) {
			return Optional.empty();
		}
		String normalizedRole = fileRole.trim().toUpperCase(Locale.ROOT);
		return mediaSlots.stream().filter(slot -> slot.role().equals(normalizedRole)).findFirst();
	}

	private static List<MediaSlot> parseMediaSlots(Object value) {
		if (!(value instanceof List<?> values)) {
			return List.of();
		}
		List<MediaSlot> slots = new ArrayList<>();
		for (Object item : values) {
			if (!(item instanceof Map<?, ?> map)) {
				continue;
			}
			String role = stringValue(map.get("role"));
			if (role == null) {
				continue;
			}
			String normalizedRole = role.toUpperCase(Locale.ROOT);
			String label = stringValue(map.get("label"));
			int maxCount = map.get("maxCount") instanceof Number number
					? Math.max(1, number.intValue()) : 1;
			slots.add(new MediaSlot(
					normalizedRole,
					label == null ? defaultMediaLabel(normalizedRole) : label,
					parseStrings(map.get("accept")),
					maxCount));
		}
		return List.copyOf(slots);
	}

	private static List<String> parseStrings(Object value) {
		if (!(value instanceof List<?> values)) {
			return List.of();
		}
		return values.stream()
				.map(PageSectionTypeDefinition::stringValue)
				.filter(item -> item != null && !item.isBlank())
				.toList();
	}

	private static String stringValue(Object value) {
		return value instanceof String text && !text.isBlank() ? text.trim() : null;
	}

	private static Map<String, Object> copyMap(Map<String, Object> value) {
		return value == null ? Map.of() : Map.copyOf(value);
	}

	private static String defaultMediaLabel(String role) {
		return switch (role) {
			case "BACKGROUND_IMAGE" -> "背景图片";
			case "BACKGROUND_VIDEO" -> "背景视频";
			case "VIDEO_POSTER" -> "视频封面";
			case "MOBILE_IMAGE" -> "移动端图片";
			case "CONTENT_IMAGE" -> "正文配图";
			default -> role;
		};
	}

	public record MediaSlot(
			String role,
			String label,
			List<String> acceptedMimeTypes,
			int maxCount) {

		public boolean accepts(String mimeType) {
			if (mimeType == null || mimeType.isBlank()) {
				return false;
			}
			String normalizedMimeType = mimeType.trim().toLowerCase(Locale.ROOT);
			return acceptedMimeTypes.stream().anyMatch(accepted -> {
				String normalizedAccepted = accepted.toLowerCase(Locale.ROOT);
				return normalizedAccepted.endsWith("/*")
						? normalizedMimeType.startsWith(
								normalizedAccepted.substring(0, normalizedAccepted.length() - 1))
						: normalizedMimeType.equals(normalizedAccepted);
			});
		}
	}
}
