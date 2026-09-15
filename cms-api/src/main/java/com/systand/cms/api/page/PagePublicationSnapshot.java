package com.systand.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Objects;

/** Current persistence-neutral page publication state. */
public record PagePublicationSnapshot(
        PagePublicationStatus status,
        OffsetDateTime scheduledPublishAt,
        OffsetDateTime scheduledUnpublishAt,
        OffsetDateTime publishedAt,
        OffsetDateTime firstPublishedAt,
        OffsetDateTime unpublishedAt) {

    public PagePublicationSnapshot {
        Objects.requireNonNull(status, "status");
    }

    public static PagePublicationSnapshot draft() {
        return new PagePublicationSnapshot(PagePublicationStatus.DRAFT, null, null, null, null, null);
    }
}
