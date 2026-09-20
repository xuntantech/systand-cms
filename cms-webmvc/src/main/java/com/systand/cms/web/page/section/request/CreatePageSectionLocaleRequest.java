package com.systand.cms.web.page.section.request;

import com.systand.cms.api.page.SectionTranslationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CreatePageSectionLocaleRequest {
	@NotBlank
	@Size(max = 35)
	@Pattern(regexp = "^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$")
	private String locale;

	@NotNull
	private SectionTranslationStatus translationStatus = SectionTranslationStatus.DRAFT;

	@NotNull
	private Map<String, Object> content = new HashMap<>();
}
