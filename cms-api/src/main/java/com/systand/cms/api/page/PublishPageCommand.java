package com.systand.cms.api.page;

import java.time.OffsetDateTime;

public record PublishPageCommand(OffsetDateTime scheduledUnpublishAt, Integer lockVersion) {
}
