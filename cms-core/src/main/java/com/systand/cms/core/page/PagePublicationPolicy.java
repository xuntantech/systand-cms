package com.systand.cms.core.page;

import com.systand.cms.api.page.PagePublicationDecision;
import com.systand.cms.api.page.PagePublicationSnapshot;
import com.systand.cms.api.page.PagePublicationStatus;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Pure domain policy for page review, scheduling, publication, and archival.
 * Hosts persist the returned decision with their own optimistic-lock mechanism.
 */
public final class PagePublicationPolicy {

    private static final Set<PagePublicationStatus> SUBMITTABLE = EnumSet.of(
            PagePublicationStatus.DRAFT, PagePublicationStatus.UNPUBLISHED);
    private static final Set<PagePublicationStatus> SCHEDULABLE = EnumSet.of(
            PagePublicationStatus.DRAFT,
            PagePublicationStatus.IN_REVIEW,
            PagePublicationStatus.UNPUBLISHED);
    private static final Set<PagePublicationStatus> PUBLISHABLE = EnumSet.of(
            PagePublicationStatus.DRAFT,
            PagePublicationStatus.IN_REVIEW,
            PagePublicationStatus.SCHEDULED,
            PagePublicationStatus.UNPUBLISHED);
    private static final Set<PagePublicationStatus> ARCHIVABLE = EnumSet.of(
            PagePublicationStatus.DRAFT,
            PagePublicationStatus.IN_REVIEW,
            PagePublicationStatus.SCHEDULED,
            PagePublicationStatus.UNPUBLISHED);

    private final Clock clock;

    public PagePublicationPolicy() {
        this(Clock.systemUTC());
    }

    public PagePublicationPolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public PagePublicationDecision submitForReview(PagePublicationSnapshot current) {
        requireStatus(current, SUBMITTABLE, "PAGE_NOT_SUBMITTABLE");
        return decision(current, PagePublicationStatus.IN_REVIEW, null, null,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    public PagePublicationDecision withdrawReview(PagePublicationSnapshot current) {
        requireStatus(current, Set.of(PagePublicationStatus.IN_REVIEW), "PAGE_NOT_IN_REVIEW");
        return decision(current, editableStatus(current), null, null,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    public PagePublicationDecision schedule(
            PagePublicationSnapshot current,
            OffsetDateTime publishAt,
            OffsetDateTime unpublishAt) {
        requireStatus(current, SCHEDULABLE, "PAGE_NOT_SCHEDULABLE");
        OffsetDateTime now = now();
        if (publishAt == null || !publishAt.isAfter(now)) {
            throw invalid("PAGE_INVALID_SCHEDULE", "scheduledPublishAt must be in the future");
        }
        if (unpublishAt != null && !unpublishAt.isAfter(publishAt)) {
            throw invalid("PAGE_INVALID_SCHEDULE", "scheduledUnpublishAt must be after scheduledPublishAt");
        }
        return decision(current, PagePublicationStatus.SCHEDULED, publishAt, unpublishAt,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    public PagePublicationDecision cancelSchedule(PagePublicationSnapshot current) {
        requireStatus(current, Set.of(PagePublicationStatus.SCHEDULED), "PAGE_NOT_SCHEDULED");
        return decision(current, editableStatus(current), null, null,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    public PagePublicationDecision publish(
            PagePublicationSnapshot current, OffsetDateTime requestedUnpublishAt) {
        requireStatus(current, PUBLISHABLE, "PAGE_NOT_PUBLISHABLE");
        OffsetDateTime publishedAt = now();
        OffsetDateTime unpublishAt = requestedUnpublishAt;
        if (unpublishAt == null && current.status() == PagePublicationStatus.SCHEDULED) {
            unpublishAt = current.scheduledUnpublishAt();
        }
        if (unpublishAt != null && !unpublishAt.isAfter(publishedAt)) {
            throw invalid("PAGE_INVALID_UNPUBLISH_TIME", "scheduledUnpublishAt must be after publication time");
        }
        OffsetDateTime firstPublishedAt = current.firstPublishedAt() == null
                ? publishedAt : current.firstPublishedAt();
        return decision(current, PagePublicationStatus.PUBLISHED, null, unpublishAt,
                publishedAt, firstPublishedAt, null);
    }

    public PagePublicationDecision unpublish(PagePublicationSnapshot current) {
        requireStatus(current, Set.of(PagePublicationStatus.PUBLISHED), "PAGE_NOT_PUBLISHED");
        return decision(current, PagePublicationStatus.UNPUBLISHED, null, null,
                current.publishedAt(), current.firstPublishedAt(), now());
    }

    public PagePublicationDecision archive(PagePublicationSnapshot current) {
        requireStatus(current, ARCHIVABLE, "PAGE_NOT_ARCHIVABLE");
        return decision(current, PagePublicationStatus.ARCHIVED, null, null,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    public PagePublicationDecision restore(PagePublicationSnapshot current) {
        requireStatus(current, Set.of(PagePublicationStatus.ARCHIVED), "PAGE_NOT_ARCHIVED");
        return decision(current, editableStatus(current), null, null,
                current.publishedAt(), current.firstPublishedAt(), current.unpublishedAt());
    }

    private static PagePublicationStatus editableStatus(PagePublicationSnapshot current) {
        return current.firstPublishedAt() == null
                ? PagePublicationStatus.DRAFT : PagePublicationStatus.UNPUBLISHED;
    }

    private static void requireStatus(
            PagePublicationSnapshot current,
            Set<PagePublicationStatus> allowed,
            String code) {
        Objects.requireNonNull(current, "current");
        if (!allowed.contains(current.status())) {
            throw invalid(code, "Operation is not allowed while page status is " + current.status());
        }
    }

    private static PagePublicationDecision decision(
            PagePublicationSnapshot ignored,
            PagePublicationStatus status,
            OffsetDateTime scheduledPublishAt,
            OffsetDateTime scheduledUnpublishAt,
            OffsetDateTime publishedAt,
            OffsetDateTime firstPublishedAt,
            OffsetDateTime unpublishedAt) {
        return new PagePublicationDecision(
                status,
                scheduledPublishAt,
                scheduledUnpublishAt,
                publishedAt,
                firstPublishedAt,
                unpublishedAt);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static CmsDomainException invalid(String code, String message) {
        return new CmsDomainException(code, message);
    }
}
