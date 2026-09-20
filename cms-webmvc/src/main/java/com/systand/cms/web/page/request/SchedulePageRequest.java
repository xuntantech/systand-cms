package com.systand.cms.web.page.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SchedulePageRequest {
	@NotNull
	@Future
	private OffsetDateTime scheduledPublishAt;

	@Future
	private OffsetDateTime scheduledUnpublishAt;

	@NotNull
	@Min(1)
	private Integer lockVersion;
}
