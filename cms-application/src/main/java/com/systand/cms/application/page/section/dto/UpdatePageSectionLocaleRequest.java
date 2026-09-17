package com.systand.cms.application.page.section.dto;

import com.systand.cms.api.page.SectionTranslationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdatePageSectionLocaleRequest {
	@NotNull
	private SectionTranslationStatus translationStatus;

	@NotNull
	private Map<String, Object> content = new HashMap<>();

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
