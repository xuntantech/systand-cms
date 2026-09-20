package com.systand.cms.web.page.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CreatePageLocaleRequest {
	@NotBlank
	@Size(max = 35)
	@Pattern(regexp = "^[A-Za-z]{2,3}(-[A-Za-z0-9]{2,8})*$")
	private String locale;

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
}
