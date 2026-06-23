package com.yongoh.agenthub_backend.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.yongoh.agenthub_backend.global.exception.GlobalExceptionHandler.ApiException;

class PostImageStorageServiceTest {
	@TempDir
	Path tempDirectory;

	@Test
	void storesAndLoadsImageWithGeneratedFilename() throws Exception {
		PostImageStorageService service = new PostImageStorageService(tempDirectory.toString());
		MockMultipartFile image = new MockMultipartFile(
			"image",
			"example.png",
			"image/png",
			new byte[] {1, 2, 3}
		);

		String filename = service.store(image);

		assertThat(filename).endsWith(".png");
		assertThat(Files.readAllBytes(tempDirectory.resolve(filename))).containsExactly(1, 2, 3);
		assertThat(service.load(filename).exists()).isTrue();
		assertThat(service.contentType(filename)).isEqualTo("image/png");
	}

	@Test
	void rejectsUnsupportedFileType() {
		PostImageStorageService service = new PostImageStorageService(tempDirectory.toString());
		MockMultipartFile file = new MockMultipartFile("image", "note.txt", "text/plain", new byte[] {1});

		assertThatThrownBy(() -> service.store(file))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("JPEG");
	}

	@Test
	void acceptsAlternativeJpgContentType() {
		PostImageStorageService service = new PostImageStorageService(tempDirectory.toString());
		MockMultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpg", new byte[] {1});

		assertThat(service.store(image)).endsWith(".jpg");
	}

	@Test
	void rejectsPathOutsideImageDirectory() {
		PostImageStorageService service = new PostImageStorageService(tempDirectory.toString());

		assertThatThrownBy(() -> service.load("../secret.png"))
			.isInstanceOf(ApiException.class)
			.hasMessageContaining("잘못된 이미지 경로");
	}
}
