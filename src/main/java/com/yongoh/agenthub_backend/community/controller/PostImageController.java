package com.yongoh.agenthub_backend.community.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.yongoh.agenthub_backend.community.service.PostImageStorageService;

@RestController
public class PostImageController {
	private final PostImageStorageService imageStorageService;

	public PostImageController(PostImageStorageService imageStorageService) {
		this.imageStorageService = imageStorageService;
	}

	@GetMapping("/api/images/{filename:.+}")
	public ResponseEntity<Resource> findImage(@PathVariable String filename) {
		Resource image = imageStorageService.load(filename);
		String contentType = imageStorageService.contentType(filename);
		return ResponseEntity.ok()
			.cacheControl(CacheControl.noCache())
			.contentType(MediaType.parseMediaType(contentType))
			.body(image);
	}
}
