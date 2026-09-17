package com.systand.cms.api.media;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Host integration port for media lookup and binding. */
public interface CmsMediaPort {

    CmsMediaFile requireFile(UUID tenantId, UUID siteId, UUID fileId);

    List<CmsMediaBinding> findBindings(
            UUID tenantId, String resourceType, UUID resourceId, String role);

    CmsMediaBinding bind(
            UUID tenantId,
            UUID fileId,
            String resourceType,
            UUID resourceId,
            String role,
            int sortOrder,
            boolean primary,
            Map<String, Object> metadata);

    void unbind(UUID tenantId, String resourceType, UUID resourceId, UUID bindingId, long lockVersion);

    /** Remove media links when a CMS resource is deleted. */
    void deleteResourceBindings(UUID tenantId, String resourceType, UUID resourceId);
}
