package com.xuntan.cms.core.page;

/** A host-neutral rejection of an invalid CMS domain operation. */
public final class CmsDomainException extends RuntimeException {

    private final String code;

    public CmsDomainException(String code, String message) {
        super(message);
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be blank");
        }
        this.code = code;
    }

    public String code() {
        return code;
    }
}
