package com.systand.cms.api.error;

/** Stable CMS error identifiers independent of any host's response envelope. */
public enum CmsErrorCode {
    INVALID_ARGUMENT("CMS_INVALID_ARGUMENT", 400),
    UNAUTHENTICATED("CMS_UNAUTHENTICATED", 401),
    FORBIDDEN("CMS_FORBIDDEN", 403),
    NOT_FOUND("CMS_NOT_FOUND", 404),
    CONFLICT("CMS_CONFLICT", 409),
    MEDIA_UNAVAILABLE("CMS_MEDIA_UNAVAILABLE", 503);

    private final String code;
    private final int httpStatus;

    CmsErrorCode(String code, int httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String code() {
        return code;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
