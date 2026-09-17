package com.systand.cms.application.page.dto;

import com.systand.cms.api.page.PageKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CreatePageRequest {
	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_-][a-z0-9]+)*$")
	private String code;

	@NotBlank
	@Size(max = 512)
	@Pattern(regexp = "^/$|^/[a-z0-9]+([/-][a-z0-9]+)*$")
	private String routePath;

	private PageKind pageKind = PageKind.STANDARD;

	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_-][a-z0-9]+)*$")
	private String templateCode = "default";

	private Boolean isHome = false;

	@Min(0)
	private Integer sortOrder = 0;

	private Map<String, Object> layoutSettings = new HashMap<>();
}
