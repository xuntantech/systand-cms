package com.systand.cms.web.page.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PublishPageRequest {
	private OffsetDateTime scheduledUnpublishAt;

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
