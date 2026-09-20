package com.systand.cms.web.page.section.controller;

import com.systand.cms.api.page.CmsPageSectionService;
import com.systand.cms.api.page.PageSectionVO;
import com.systand.cms.web.page.converter.CmsPageWebConverter;
import com.systand.cms.web.page.section.request.CreatePageSectionRequest;
import com.systand.cms.web.page.section.request.UpdatePageSectionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
@RequestMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites/{siteId}/pages/{pageId}/sections")
public class PageSectionController {
	private final CmsPageSectionService pageSectionService;
	private final CmsPageWebConverter converter;

	@GetMapping
	public List<PageSectionVO> getSections(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam(required = false) Boolean visible,
			@RequestParam(required = false)
			@Pattern(regexp = "^[a-z0-9]+([_][a-z0-9]+)*$") String sectionType) {
		return pageSectionService.getSections(siteId, pageId, visible, sectionType);
	}

	@GetMapping("/{sectionId}")
	public PageSectionVO getSection(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId) {
		return pageSectionService.getSection(siteId, pageId, sectionId);
	}

	@PostMapping
	public ResponseEntity<PageSectionVO> createSection(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@Valid @RequestBody CreatePageSectionRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(pageSectionService.createSection(siteId, pageId, converter.toCommand(request)));
	}

	@PutMapping("/{sectionId}")
	public PageSectionVO updateSection(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@Valid @RequestBody UpdatePageSectionRequest request) {
		return pageSectionService.updateSection(siteId, pageId, sectionId, converter.toCommand(request));
	}

	@DeleteMapping("/{sectionId}")
	public ResponseEntity<Void> deleteSection(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@PathVariable UUID sectionId,
			@RequestParam @Min(1) Integer lockVersion) {
		pageSectionService.deleteSection(siteId, pageId, sectionId, lockVersion);
		return ResponseEntity.noContent().build();
	}
}
