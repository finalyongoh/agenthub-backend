# Repository Collect Batch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the Spring Batch repository collection flow so AgentHub can discover Agent-related repositories, refresh known repositories, store README and shallow file-tree snapshots, then mark collected repositories for later summary and analysis.

**Architecture:** Keep the existing `GithubReadmeSyncJobConfig` and `RepositorySyncService` shape, but evolve it into a repository collection job. Discovery remains query-registry based through `github.sync.queries`; refresh adds already known active repositories to the same batch run; snapshot adds shallow GitHub tree data beside README data. Summary/analysis generation remains separate and asynchronous.

**Tech Stack:** Java 21, Spring Boot 4, Spring Batch, Spring Data JPA, RestClient, PostgreSQL/H2 tests.

---

## Source Requirements

- `finalyongoh/docs` F17: collect GitHub repository data periodically.
- `finalyongoh/docs` F18/F21: batch flow collects metadata and snapshots first; summary/analysis are asynchronous and read stored data.
- `finalyongoh/agenttrace`: analyzer input is repository metadata + README + shallow `file_tree`.

## File Structure

- Modify: `src/main/java/com/yongoh/agenthub_backend/batch/GithubReadmeSyncJobConfig.java`
  - Rename step intent from README-only to repository collection.
  - Add refresh/snapshot semantics while keeping existing bean compatibility.
- Modify: `src/main/java/com/yongoh/agenthub_backend/repository/service/RepositorySyncService.java`
  - Merge discovered candidates with known repositories.
  - Fetch README and shallow file tree.
  - Queue pending analysis only after snapshot data exists.
- Modify: `src/main/java/com/yongoh/agenthub_backend/github/GithubClient.java`
  - Add repository metadata fetch by `owner/repo`.
  - Add shallow tree fetch by `owner/repo/defaultBranch`.
- Modify: `src/main/java/com/yongoh/agenthub_backend/github/GithubRepositorySearchService.java`
  - Add refresh helper for known repositories.
- Create: `src/main/java/com/yongoh/agenthub_backend/github/dto/GithubFileTreeItemDto.java`
  - Store `path` and `type` from GitHub tree API.
- Create: `src/main/java/com/yongoh/agenthub_backend/repository/model/RepositoryFileTree.java`
  - Store shallow file-tree JSON snapshot per repository.
- Create: `src/main/java/com/yongoh/agenthub_backend/repository/repository/RepositoryFileTreeJpaRepository.java`
  - Load/store file-tree snapshots.
- Test: `src/test/java/com/yongoh/agenthub_backend/repository/service/RepositorySyncServiceTest.java`
  - Unit-level service tests with fake collaborators where possible.

## Tasks

### Task 1: Refresh Known Repositories

- [ ] Write failing test proving `RepositorySyncService` includes existing active repositories in a collect run.
- [ ] Run targeted test and verify it fails because refresh is missing.
- [ ] Add repository query method for active known repositories.
- [ ] Merge discovered and known repositories by `githubId`.
- [ ] Run targeted test and verify it passes.

### Task 2: Store Shallow File Tree Snapshot

- [ ] Write failing test proving a collected repository stores README plus shallow file-tree paths.
- [ ] Run targeted test and verify it fails because file-tree storage is missing.
- [ ] Add GitHub tree DTO and client method.
- [ ] Add `RepositoryFileTree` entity and repository.
- [ ] Fetch and save shallow tree during snapshot step.
- [ ] Run targeted test and verify it passes.

### Task 3: Queue Analysis From Snapshot

- [ ] Write failing test proving analysis is queued only when repository has README and file-tree snapshot.
- [ ] Run targeted test and verify it fails because queueing is missing.
- [ ] Add pending analysis creation after snapshot is complete.
- [ ] Avoid duplicate pending analysis rows for same repository.
- [ ] Run targeted test and verify it passes.

### Task 4: Batch Job Wiring

- [ ] Update batch step names/comments to repository collection vocabulary.
- [ ] Ensure scheduler/admin launcher still starts the same job bean.
- [ ] Run full test suite with `rtk ./gradlew test`.

## Self-Review

- No local product documents under `docs/` are recreated.
- No GitHub API call happens in user request paths.
- README, metadata, and shallow file tree are stored before analysis.
- 2차 analysis remains asynchronous; this plan does not implement LLM summary generation.
