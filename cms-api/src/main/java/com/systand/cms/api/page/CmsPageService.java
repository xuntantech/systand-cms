package com.systand.cms.api.page;

import java.util.List;
import java.util.UUID;

public interface CmsPageService {
    List<PageVO> getPages(UUID siteId, PagePublicationStatus status, PageKind pageKind, String keyword);
    PageVO getPage(UUID siteId, UUID pageId);
    PageVO createPage(UUID siteId, CreatePageCommand command);
    PageVO updatePage(UUID siteId, UUID pageId, UpdatePageCommand command);
    void deletePage(UUID siteId, UUID pageId, Integer lockVersion);
    PageVO submitForReview(UUID siteId, UUID pageId, Integer lockVersion);
    PageVO withdrawReview(UUID siteId, UUID pageId, Integer lockVersion);
    PageVO schedulePage(UUID siteId, UUID pageId, SchedulePageCommand command);
    PageVO cancelSchedule(UUID siteId, UUID pageId, Integer lockVersion);
    PageVO publishPage(UUID siteId, UUID pageId, PublishPageCommand command);
    PageVO unpublishPage(UUID siteId, UUID pageId, Integer lockVersion);
    PageVO archivePage(UUID siteId, UUID pageId, Integer lockVersion);
}
