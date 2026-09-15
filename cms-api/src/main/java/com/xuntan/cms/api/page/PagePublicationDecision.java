package com.xuntan.cms.api.page;

import java.time.OffsetDateTime;
import java.util.Objects;

/** Validated state to persist after a page publication command. */
public record PagePublicationDecision(
        PagePublicationStatus status,
        OffsetDateTime scheduledPublishAt,
        OffsetDateTime scheduledUnpublishAt,
        OffsetDateTime publishedAt,
        OffsetDateTime firstPublishedAt,
        OffsetDateTime unpublishedAt) {

    public PagePublicationDecision {
        Objects.requireNonNull(status, "status");
    }

    public PagePublicationSnapshot toSnapshot() {
        return new PagePublicationSnapshot(
                status,
                scheduledPublishAt,
                scheduledUnpublishAt,
                publishedAt,
                firstPublishedAt,
                unpublishedAt);
    }
}
