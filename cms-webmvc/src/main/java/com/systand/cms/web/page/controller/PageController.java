package com.systand.cms.web.page.controller;

import com.systand.cms.api.page.CmsPageService;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.api.page.PageVO;
import com.systand.cms.web.page.converter.CmsPageWebConverter;
import com.systand.cms.web.page.request.CreatePageRequest;
import com.systand.cms.web.page.request.PublishPageRequest;
import com.systand.cms.web.page.request.SchedulePageRequest;
import com.systand.cms.web.page.request.UpdatePageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("${systand.cms.api-prefix:/v1/cms}" + "/sites/{siteId}/pages")
public class PageController {
	private final CmsPageService pageService;
	private final CmsPageWebConverter converter;

	@GetMapping
	public List<PageVO> getPages(
			@PathVariable UUID siteId,
			@RequestParam(required = false) PagePublicationStatus status,
			@RequestParam(required = false) PageKind pageKind,
			@RequestParam(required = false) String keyword) {
		return pageService.getPages(siteId, status, pageKind, keyword);
	}

	@GetMapping("/{pageId}")
	public PageVO getPage(@PathVariable UUID siteId, @PathVariable UUID pageId) {
		return pageService.getPage(siteId, pageId);
	}

	@PostMapping
	public ResponseEntity<PageVO> createPage(
			@PathVariable UUID siteId,
			@Valid @RequestBody CreatePageRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(pageService.createPage(siteId, converter.toCommand(request)));
	}

	@PutMapping("/{pageId}")
	public PageVO updatePage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@Valid @RequestBody UpdatePageRequest request) {
		return pageService.updatePage(siteId, pageId, converter.toCommand(request));
	}

	@DeleteMapping("/{pageId}")
	public ResponseEntity<Void> deletePage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		pageService.deletePage(siteId, pageId, lockVersion);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{pageId}/submit-review")
	public PageVO submitForReview(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		return pageService.submitForReview(siteId, pageId, lockVersion);
	}

	@PatchMapping("/{pageId}/withdraw-review")
	public PageVO withdrawReview(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		return pageService.withdrawReview(siteId, pageId, lockVersion);
	}

	@PostMapping("/{pageId}/schedule")
	public PageVO schedulePage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@Valid @RequestBody SchedulePageRequest request) {
		return pageService.schedulePage(siteId, pageId, converter.toCommand(request));
	}

	@PatchMapping("/{pageId}/cancel-schedule")
	public PageVO cancelSchedule(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		return pageService.cancelSchedule(siteId, pageId, lockVersion);
	}

	@PostMapping("/{pageId}/publish")
	public PageVO publishPage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@Valid @RequestBody PublishPageRequest request) {
		return pageService.publishPage(siteId, pageId, converter.toCommand(request));
	}

	@PatchMapping("/{pageId}/unpublish")
	public PageVO unpublishPage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		return pageService.unpublishPage(siteId, pageId, lockVersion);
	}

	@PatchMapping("/{pageId}/archive")
	public PageVO archivePage(
			@PathVariable UUID siteId,
			@PathVariable UUID pageId,
			@RequestParam @Min(1) Integer lockVersion) {
		return pageService.archivePage(siteId, pageId, lockVersion);
	}
}
