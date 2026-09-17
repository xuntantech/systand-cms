package com.systand.cms.web.page.section.controller;

import com.systand.cms.application.page.section.dto.CreatePageSectionLocaleRequest;
import com.systand.cms.application.page.section.dto.UpdatePageSectionLocaleRequest;
import com.systand.cms.application.page.section.service.PageSectionLocaleService;
import com.systand.cms.application.page.section.vo.PageSectionLocaleVO;
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
@RequestMapping("${systand.cms.api-prefix:/v1/cms}"
		+ "/sites/{siteId}/pages/{pageId}/sections/{sectionId}/locales")
public class PageSectionLocaleController {
	private final PageSectionLocaleService pageSectionLocaleService;

	@GetMapping
	public List<PageSectionLocaleVO> getSectionLocales(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId) {
		return pageSectionLocaleService.getSectionLocales(siteId, pageId, sectionId);
	}

	@GetMapping("/{localeId}")
	public PageSectionLocaleVO getSectionLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@PathVariable UUID localeId) {
		return pageSectionLocaleService.getSectionLocale(siteId, pageId, sectionId, localeId);
	}

	@PostMapping
	public ResponseEntity<PageSectionLocaleVO> createSectionLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@Valid @RequestBody CreatePageSectionLocaleRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(
				pageSectionLocaleService.createSectionLocale(siteId, pageId, sectionId, request));
	}

	@PutMapping("/{localeId}")
	public PageSectionLocaleVO updateSectionLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@PathVariable UUID localeId,
			@Valid @RequestBody UpdatePageSectionLocaleRequest request) {
		return pageSectionLocaleService.updateSectionLocale(
				siteId, pageId, sectionId, localeId, request);
	}

	@DeleteMapping("/{localeId}")
	public ResponseEntity<Void> deleteSectionLocale(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@PathVariable UUID localeId,
			@RequestParam @Min(1) Integer lockVersion) {
		pageSectionLocaleService.deleteSectionLocale(
				siteId, pageId, sectionId, localeId, lockVersion);
		return ResponseEntity.noContent().build();
	}
}
