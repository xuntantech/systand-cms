package com.systand.cms.application.page.section.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdatePageSectionRequest {
	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_][a-z0-9]+)*$")
	private String sectionKey;

	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_][a-z0-9]+)*$")
	private String sectionType;

	@Size(max = 100)
	private String adminLabel;

	@Size(max = 64)
	@Pattern(regexp = "^$|^[a-z][a-z0-9]*(-[a-z0-9]+)*$")
	private String anchorId;

	@NotNull
	@Min(0)
	private Integer sortOrder;

	@NotNull
	private Boolean isVisible;

	@NotNull
	private Map<String, Object> settings = new HashMap<>();

	@NotNull
	@Min(1)
	private Integer schemaVersion;

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
