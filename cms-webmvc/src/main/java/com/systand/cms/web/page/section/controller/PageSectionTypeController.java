package com.systand.cms.web.page.section.controller;

import com.systand.cms.application.page.section.service.PageSectionTypeService;
import com.systand.cms.application.page.section.vo.PageSectionTypeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 返回当前登录租户真正已授权且启用的页面区块类型。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${systand.cms.api-prefix:/v1/cms}" + "/section-types")
public class PageSectionTypeController {
	private final PageSectionTypeService pageSectionTypeService;

	@GetMapping
	public List<PageSectionTypeVO> getSectionTypes() {
		return pageSectionTypeService.getAvailableSectionTypes();
	}
}
