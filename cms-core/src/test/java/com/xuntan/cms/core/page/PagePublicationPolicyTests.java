package com.xuntan.cms.core.page;

import com.xuntan.cms.api.page.PagePublicationDecision;
import com.xuntan.cms.api.page.PagePublicationSnapshot;
import com.xuntan.cms.api.page.PagePublicationStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagePublicationPolicyTests {

    private static final Instant NOW = Instant.parse("2026-09-15T01:00:00Z");
    private final PagePublicationPolicy policy =
            new PagePublicationPolicy(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void publishesDraftAndSetsFirstPublicationTime() {
        PagePublicationDecision result = policy.publish(PagePublicationSnapshot.draft(), null);

        assertEquals(PagePublicationStatus.PUBLISHED, result.status());
        assertEquals(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC), result.publishedAt());
        assertEquals(result.publishedAt(), result.firstPublishedAt());
    }

    @Test
    void preservesFirstPublicationTimeWhenRepublishing() {
        OffsetDateTime firstPublishedAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        PagePublicationSnapshot current = new PagePublicationSnapshot(
                PagePublicationStatus.UNPUBLISHED, null, null, null, firstPublishedAt, null);

        PagePublicationDecision result = policy.publish(current, null);

        assertEquals(firstPublishedAt, result.firstPublishedAt());
    }

    @Test
    void rejectsInvalidScheduleWindow() {
        OffsetDateTime publishAt = OffsetDateTime.parse("2026-09-16T00:00:00Z");
        OffsetDateTime unpublishAt = publishAt.minusMinutes(1);

        CmsDomainException exception = assertThrows(
                CmsDomainException.class,
                () -> policy.schedule(PagePublicationSnapshot.draft(), publishAt, unpublishAt));

        assertEquals("PAGE_INVALID_SCHEDULE", exception.code());
    }

    @Test
    void unpublishesOnlyPublishedPages() {
        assertThrows(CmsDomainException.class, () -> policy.unpublish(PagePublicationSnapshot.draft()));

        PagePublicationDecision published = policy.publish(PagePublicationSnapshot.draft(), null);
        PagePublicationDecision unpublished = policy.unpublish(published.toSnapshot());
        assertEquals(PagePublicationStatus.UNPUBLISHED, unpublished.status());
        assertNotNull(unpublished.unpublishedAt());
    }
}
