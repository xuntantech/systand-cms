package com.systand.cms.web.page.controller;

import com.systand.cms.web.error.CmsErrorResponse;
import com.systand.cms.core.error.CmsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps shared CMS failures without borrowing platform-common's Result format. */
@RestControllerAdvice(basePackages = "com.systand.cms.web")
public class CmsErrorHandler {
	@ExceptionHandler(CmsException.class)
	public ResponseEntity<CmsErrorResponse> handle(CmsException exception) {
		return ResponseEntity.status(exception.errorCode().httpStatus())
				.body(new CmsErrorResponse(exception.errorCode().code(), exception.getMessage()));
	}
}
