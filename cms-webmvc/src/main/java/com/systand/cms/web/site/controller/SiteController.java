package com.systand.cms.web.site.controller;

import com.systand.cms.persistence.mybatis.site.dataobject.SiteDO;
import com.systand.cms.application.site.service.SiteService;
import com.systand.cms.application.site.service.CreateSiteRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SiteController {
	private final SiteService siteService;

	@GetMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites")
	public List<SiteDO> getSitesByTenant() {
		List<SiteDO> sites = siteService.getSitesByTenant();
		return sites;
	}

	@PostMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites")
	public ResponseEntity<SiteDO> createSite(@Valid @RequestBody CreateSiteRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(siteService.createSite(request));
	}
}
