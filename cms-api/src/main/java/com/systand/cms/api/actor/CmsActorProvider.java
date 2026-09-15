package com.systand.cms.api.actor;

/** Supplies the trusted actor for the current CMS operation. */
@FunctionalInterface
public interface CmsActorProvider {
    CmsActor requireActor();
}
