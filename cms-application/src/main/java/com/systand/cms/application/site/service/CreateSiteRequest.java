package com.systand.cms.application.site.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Client-owned fields only; tenant and audit identity are resolved by the host. */
public record CreateSiteRequest(
        @NotBlank @Pattern(regexp = "[a-z0-9]+([_-][a-z0-9]+)*") @Size(max = 64) String code,
        @NotBlank @Size(max = 120) String name,
        @NotBlank String defaultLocale,
        List<String> enabledLocales) {
}
