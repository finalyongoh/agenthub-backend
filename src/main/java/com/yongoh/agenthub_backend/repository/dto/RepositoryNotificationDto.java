package com.yongoh.agenthub_backend.repository.dto;

import java.time.Instant;
import java.util.UUID;

import com.yongoh.agenthub_backend.repository.model.RepositoryNotification;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RepositoryNotificationDto {
	private UUID id;
	private UUID repositoryId;
	private String repositoryFullName;
	private String type;
	private String message;
	private String oldSha;
	private String newSha;
	private boolean read;
	private Instant readAt;
	private Instant createdAt;

	public static RepositoryNotificationDto from(RepositoryNotification notification) {
		return new RepositoryNotificationDto(
			notification.getId(),
			notification.getRepository().getId(),
			notification.getRepository().getFullName(),
			notification.getType(),
			notification.getMessage(),
			notification.getOldSha(),
			notification.getNewSha(),
			notification.isRead(),
			notification.getReadAt(),
			notification.getCreatedAt()
		);
	}
}
