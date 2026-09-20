package com.systand.cms.api.page;

import java.time.OffsetDateTime;

public record SchedulePageCommand(
        OffsetDateTime scheduledPublishAt,
        OffsetDateTime scheduledUnpublishAt,
        Integer lockVersion) {
}
