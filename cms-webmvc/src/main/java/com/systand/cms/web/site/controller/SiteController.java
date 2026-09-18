package com.systand.cms.web.site.controller;

import com.systand.cms.api.site.CmsSiteVO;
import com.systand.cms.api.site.CmsSiteOperations;
import com.systand.cms.api.site.CmsCreateSiteCommand;
import com.systand.cms.web.site.dto.CreateSiteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "systand.cms.web", name = "site-enabled",
        havingValue = "true", matchIfMissing = true)
public class SiteController {
	private final CmsSiteOperations siteService;

	@GetMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites")
	public List<CmsSiteVO> getSitesByTenant() {
		List<CmsSiteVO> sites = siteService.getSitesByTenant();
		return sites;
	}

	@PostMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites")
	public ResponseEntity<CmsSiteVO> createSite(@Valid @RequestBody CreateSiteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(siteService.createSite(
				new CmsCreateSiteCommand(request.code(), request.name(),
						request.defaultLocale(), request.enabledLocales())));
	}
}
