package com.yongoh.agenthub_backend.community.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;

@Service
public class PostImageStorageService {
	private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
	private static final Map<String, String> EXTENSIONS = Map.of(
		"image/jpeg", ".jpg",
		"image/jpg", ".jpg",
		"image/png", ".png",
		"image/gif", ".gif",
		"image/webp", ".webp"
	);

	private final Path imageDirectory;

	public PostImageStorageService(@Value("${agenthub.images.directory:img}") String imageDirectory) {
		this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
	}

	public String store(MultipartFile image) {
		if (image == null || image.isEmpty()) {
			return null;
		}
		if (image.getSize() > MAX_IMAGE_SIZE) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE_TOO_LARGE", "이미지는 10MB 이하만 업로드할 수 있습니다.");
		}

		String extension = EXTENSIONS.get(image.getContentType());
		if (extension == null) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE_TYPE_INVALID", "JPG, JPEG, PNG, GIF, WebP 이미지만 업로드할 수 있습니다.");
		}

		String filename = UUID.randomUUID() + extension;
		try {
			Files.createDirectories(imageDirectory);
			Files.copy(image.getInputStream(), imageDirectory.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
			return filename;
		} catch (IOException exception) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_SAVE_FAILED", "이미지 저장에 실패했습니다.");
		}
	}

	public Resource load(String filename) {
		Path image = resolve(filename);
		if (!Files.isRegularFile(image)) {
			throw new ApiException(HttpStatus.NOT_FOUND, "IMAGE_NOT_FOUND", "이미지를 찾을 수 없습니다.");
		}
		try {
			return new UrlResource(image.toUri());
		} catch (IOException exception) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_READ_FAILED", "이미지를 불러오지 못했습니다.");
		}
	}

	public String contentType(String filename) {
		Path image = resolve(filename);
		try {
			String contentType = Files.probeContentType(image);
			return contentType == null ? "application/octet-stream" : contentType;
		} catch (IOException exception) {
			return "application/octet-stream";
		}
	}

	private Path resolve(String filename) {
		Path image = imageDirectory.resolve(filename).normalize();
		if (!image.startsWith(imageDirectory)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "IMAGE_PATH_INVALID", "잘못된 이미지 경로입니다.");
		}
		return image;
	}
}
