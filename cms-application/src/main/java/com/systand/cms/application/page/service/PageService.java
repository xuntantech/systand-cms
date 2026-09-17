package com.systand.cms.application.page.service;

import com.systand.cms.application.page.dto.CreatePageRequest;
import com.systand.cms.application.page.dto.PublishPageRequest;
import com.systand.cms.application.page.dto.SchedulePageRequest;
import com.systand.cms.application.page.dto.UpdatePageRequest;
import com.systand.cms.api.page.PageKind;
import com.systand.cms.api.page.PagePublicationStatus;
import com.systand.cms.application.page.vo.PageVO;

import java.util.List;
import java.util.UUID;

public interface PageService {
	List<PageVO> getPages(UUID siteId, PagePublicationStatus status, PageKind pageKind, String keyword);

	PageVO getPage(UUID siteId, UUID pageId);

	PageVO createPage(UUID siteId, CreatePageRequest request);

	PageVO updatePage(UUID siteId, UUID pageId, UpdatePageRequest request);

	void deletePage(UUID siteId, UUID pageId, Integer lockVersion);

	PageVO submitForReview(UUID siteId, UUID pageId, Integer lockVersion);

	PageVO withdrawReview(UUID siteId, UUID pageId, Integer lockVersion);

	PageVO schedulePage(UUID siteId, UUID pageId, SchedulePageRequest request);

	PageVO cancelSchedule(UUID siteId, UUID pageId, Integer lockVersion);

	PageVO publishPage(UUID siteId, UUID pageId, PublishPageRequest request);

	PageVO unpublishPage(UUID siteId, UUID pageId, Integer lockVersion);

	PageVO archivePage(UUID siteId, UUID pageId, Integer lockVersion);
}
