package com.systand.cms.web.page.controller;

import com.systand.cms.application.page.dto.CreatePageLocaleRequest;
import com.systand.cms.application.page.dto.UpdatePageLocaleRequest;
import com.systand.cms.application.page.service.PageLocaleService;
import com.systand.cms.application.page.vo.PageLocaleVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites/{siteId}/pages/{pageId}/locales")
public class PageLocaleController {
	private final PageLocaleService pageLocaleService;

	@GetMapping
	public List<PageLocaleVO> getPageLocales(@PathVariable UUID siteId, @PathVariable UUID pageId) {
		return pageLocaleService.getPageLocales(siteId, pageId);
	}

	@GetMapping("/{localeId}")
	public PageLocaleVO getPageLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID localeId) {
		return pageLocaleService.getPageLocale(siteId, pageId, localeId);
	}

	@PostMapping
	public ResponseEntity<PageLocaleVO> createPageLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@Valid @RequestBody CreatePageLocaleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(pageLocaleService.createPageLocale(siteId, pageId, request));
	}

	@PutMapping("/{localeId}")
	public PageLocaleVO updatePageLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID localeId,
			@Valid @RequestBody UpdatePageLocaleRequest request) {
		return pageLocaleService.updatePageLocale(siteId, pageId, localeId, request);
	}

	@DeleteMapping("/{localeId}")
	public ResponseEntity<Void> deletePageLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID localeId,
			@RequestParam @Min(1) Integer lockVersion) {
		pageLocaleService.deletePageLocale(siteId, pageId, localeId, lockVersion);
		return ResponseEntity.noContent().build();
	}
}
