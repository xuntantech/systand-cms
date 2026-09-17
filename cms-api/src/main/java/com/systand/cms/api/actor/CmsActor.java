package com.systand.cms.api.actor;

import java.util.Objects;
import java.util.UUID;

/** Trusted actor information used for audit fields and audit events. */
public record CmsActor(UUID userId, CmsActorType type, String systemIdentifier) {

    public CmsActor {
        Objects.requireNonNull(type, "type");
        systemIdentifier = normalize(systemIdentifier);
        if ((type == CmsActorType.PLATFORM_USER || type == CmsActorType.TENANT_USER) && userId == null) {
            throw new IllegalArgumentException("A human actor requires a userId");
        }
        if (type != CmsActorType.PLATFORM_USER && type != CmsActorType.TENANT_USER && userId != null) {
            throw new IllegalArgumentException("SYSTEM and ANONYMOUS actors must not use a fabricated userId");
        }
        if (type == CmsActorType.SYSTEM && systemIdentifier == null) {
            throw new IllegalArgumentException("A SYSTEM actor requires a systemIdentifier");
        }
        if (type != CmsActorType.SYSTEM && systemIdentifier != null) {
            throw new IllegalArgumentException("Only SYSTEM actors may have a systemIdentifier");
        }
    }

    public static CmsActor human(UUID userId) {
        return tenantUser(userId);
    }

    public static CmsActor tenantUser(UUID userId) {
        return new CmsActor(Objects.requireNonNull(userId, "userId"), CmsActorType.TENANT_USER, null);
    }

    public static CmsActor platformUser(UUID userId) {
        return new CmsActor(Objects.requireNonNull(userId, "userId"), CmsActorType.PLATFORM_USER, null);
    }

    public String auditType() {
        return switch (type) {
            case PLATFORM_USER -> "platform_user";
            case TENANT_USER -> "tenant_user";
            case SYSTEM -> "system";
            case ANONYMOUS -> "anonymous";
        };
    }

    public static CmsActor system(String systemIdentifier) {
        return new CmsActor(null, CmsActorType.SYSTEM, systemIdentifier);
    }

    public static CmsActor anonymous() {
        return new CmsActor(null, CmsActorType.ANONYMOUS, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
