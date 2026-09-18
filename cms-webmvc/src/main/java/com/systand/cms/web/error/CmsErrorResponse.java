package com.systand.cms.web.error;

/** Stable CMS response envelope for its own HTTP endpoints. */
public record CmsErrorResponse(String code, String message) {
}
