package com.systand.cms.core.error;

import com.systand.cms.api.error.CmsErrorCode;

import java.util.Objects;

/** Portable CMS failure that a web adapter can map to HTTP or a host envelope. */
public final class CmsException extends RuntimeException {
    private final CmsErrorCode errorCode;

    public CmsException(CmsErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    public CmsErrorCode errorCode() {
        return errorCode;
    }
}
