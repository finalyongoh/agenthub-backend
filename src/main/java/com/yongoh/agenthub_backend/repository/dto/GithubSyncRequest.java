package com.yongoh.agenthub_backend.repository.dto;

public class GithubSyncRequest {
	private Integer limit;
	private boolean force;

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}
}
