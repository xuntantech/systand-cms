package com.systand.cms.application.page.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdatePageLocaleRequest {
	@NotBlank
	@Size(max = 255)
	private String title;

	@Size(max = 4000)
	private String summary;

	@Size(max = 255)
	private String seoTitle;

	@Size(max = 500)
	private String seoDescription;

	@NotNull
	private Map<String, Object> metadata = new HashMap<>();

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
