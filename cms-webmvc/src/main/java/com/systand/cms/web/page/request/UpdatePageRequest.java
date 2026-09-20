package com.systand.cms.web.page.request;

import com.systand.cms.api.page.PageKind;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class UpdatePageRequest {
	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_-][a-z0-9]+)*$")
	private String code;

	@NotBlank
	@Size(max = 512)
	@Pattern(regexp = "^/$|^/[a-z0-9]+([/-][a-z0-9]+)*$")
	private String routePath;

	@NotNull
	private PageKind pageKind;

	@NotBlank
	@Size(max = 64)
	@Pattern(regexp = "^[a-z0-9]+([_-][a-z0-9]+)*$")
	private String templateCode;

	@NotNull
	private Boolean isHome;

	@NotNull
	@Min(0)
	private Integer sortOrder;

	@NotNull
	private Map<String, Object> layoutSettings = new HashMap<>();

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
