package com.yongoh.agenthub_backend.repository.service;

import java.io.Serializable;

public class SyncStatistics implements Serializable {
	private static final long serialVersionUID = 1L;

	private int searchedCount;
	private int savedCount;
	private int readmeFetchedCount;
	private int agentRelatedCount;
	private int skippedCount;
	private int failedCount;

	public void addSearchedCount(int count) {
		this.searchedCount += count;
	}

	public void incrementSavedCount() {
		this.savedCount++;
	}

	public void incrementReadmeFetchedCount() {
		this.readmeFetchedCount++;
	}

	public void incrementAgentRelatedCount() {
		this.agentRelatedCount++;
	}

	public void incrementSkippedCount() {
		this.skippedCount++;
	}

	public void incrementFailedCount() {
		this.failedCount++;
	}

	public int getSearchedCount() {
		return searchedCount;
	}

	public int getSavedCount() {
		return savedCount;
	}

	public int getReadmeFetchedCount() {
		return readmeFetchedCount;
	}

	public int getAgentRelatedCount() {
		return agentRelatedCount;
	}

	public int getSkippedCount() {
		return skippedCount;
	}

	public int getFailedCount() {
		return failedCount;
	}
}
