# agenthub-backend

AgentHub API 서버. GitHub에서 AI agent 관련 오픈소스 저장소를 수집하고, README를 받아 점수를 매기고, 주간 트렌드 리포트를 만들어 [`agenthub-frontend`](https://github.com/finalyongoh/agenthub-frontend)에 제공합니다.

Spring Boot 4 · Java 21 · PostgreSQL · Spring Batch.

## 하는 일

```text
GitHub Search API → 후보 저장소 저장 → README 수집 → Agent Score 산정 → 변경 감지·알림
                                                            ↓
                                            일별 지표 스냅샷 → 주간 트렌드 리포트
```

- **수집**: GitHub Search API로 agent 관련 저장소를 찾고, 저장소 메타데이터와 README를 저장합니다.
- **선별**: 키워드 가중치와 토픽으로 agent 연관성을 판정해 무관한 저장소를 걸러냅니다. 생산성 도구나 링크 모음 저장소는 별도 규칙으로 감점합니다.
- **점수**: 기능 구현 근거, 평가 근거, 재현 가능성, 운영 품질, 사용자 채택도를 더하고 위험 신호를 감점해 0~90 범위의 Agent Score를 냅니다.
- **변경 감지**: README SHA와 설명·토픽·언어·스타·포크 등 메타데이터 변화를 기록하고, 해당 저장소를 워치리스트에 담은 사용자에게 알림을 남깁니다.
- **리포트**: 매일 지표 스냅샷을 남기고, 매주 완료된 주를 대상으로 트렌드 리포트를 생성합니다.

## 배치와 스케줄

| 작업 | 주기 | 설정 키 |
| --- | --- | --- |
| GitHub 수집 · README · 점수 (Spring Batch 3-step Job) | 매일 03:00 | `github.sync.cron` |
| 저장소 지표 스냅샷 | 매일 03:30 KST | `report.snapshot.cron` |
| 주간 트렌드 리포트 | 매주 월요일 04:00 KST | `report.weekly.cron` |

`githubReadmeSyncJob`은 `searchAgentRepositoriesStep → fetchReadmesStep → scoreAgentRepositoriesStep` 순으로 실행되며, 각 스텝은 Job ExecutionContext로 대상 저장소 ID와 통계를 넘깁니다. 관리자는 스케줄과 별개로 API로 즉시 실행할 수 있습니다.

## 도메인 구성

```text
com.yongoh.agenthub_backend
  repository/   저장소 수집·조회·점수·북마크·알림·분석 (핵심 도메인)
  community/    게시글, 댓글, 좋아요, 저장소별 Discussion
  report/       지표 스냅샷, 주간 트렌드 리포트
  github/       GitHub API 클라이언트, README·파일 트리 조회
  batch/        Spring Batch Job 정의와 스케줄러
  user/         가입, 로그인, JWT, OAuth2(Google·GitHub)
  moderation/   분석 결과·게시물 신고
  global/       보안 설정, 예외 처리, 외부 연동 프로퍼티
```

## 주요 API

```text
POST   /api/auth/signup, /api/auth/login, /api/auth/logout
GET    /api/auth/me            PATCH /api/auth/me/password

GET    /api/repositories                       목록·필터·정렬
GET    /api/repositories/{id}                  상세
POST   /api/repositories/{id}/analysis          AgentTrace 분석 요청
POST   /api/repositories/{id}/bookmark          워치리스트 추가/조회/삭제
GET    /api/bookmarks

GET    /api/trend-reports/latest, /api/trend-reports
POST   /api/trend-reports/generate

GET    /api/posts, /api/posts/{id}              커뮤니티 게시글·댓글·좋아요
GET    /api/repositories/{id}/discussions        저장소별 토론

GET    /api/notifications, /api/notifications/unread-count
POST   /api/notifications/read-all

POST   /api/admin/github/sync-agent-repositories  수집 배치 즉시 실행
GET    /api/admin/github/sync-logs                실행 이력
```

## 외부 연동

| 대상 | 용도 | 설정 |
| --- | --- | --- |
| GitHub REST API | 저장소 검색, README, 파일 트리 | `GITHUB_TOKEN` |
| [AgentTrace](https://github.com/finalyongoh/agenttrace) | 저장소 분석, README 요약, 리포트 서술 생성 | `AGENTTRACE_BASE_URL` (기본 `http://localhost:8000`) |
| OpenAI 호환 API | 영문 설명 한국어 번역 | `AGENTHUB_TRANSLATION_*`, `GMS_KEY` |

AgentTrace가 떠 있지 않으면 분석 요청은 실패하고, 수집·점수·리포트 흐름은 규칙 기반으로 계속 동작합니다.

## 로컬 실행

```bash
docker compose up -d          # PostgreSQL(pgvector:pg16) + Redis
GITHUB_TOKEN=... ./gradlew bootRun
```

`docker-compose.yml`은 PostgreSQL을 5432, Redis를 6379로 띄웁니다. 스키마는 JPA가 생성합니다.

수집을 바로 확인하려면 관리자 계정으로 배치를 호출합니다.

```bash
curl -X POST http://localhost:8080/api/admin/github/sync-agent-repositories \
  -H "Authorization: Bearer $TOKEN"
```

### 관리자 계정

신규 가입 사용자는 `USER` 권한으로 생성됩니다. `/api/admin/**`를 쓰려면 DB에서 승격한 뒤 다시 로그인합니다.

```sql
UPDATE users SET role = 'ADMIN' WHERE email = '가입한이메일@example.com';
```

```bash
docker exec -it agenthub-postgres psql -U agenthub_user -d agenthub
```

### 소셜 로그인

`oauth` 프로필을 켜고 각 OAuth 앱의 client 값을 환경변수로 넘깁니다.

```bash
cp .env.local.example .env.local
./scripts/run-oauth-local.sh
```

OAuth 앱에 등록할 callback URL:

```text
http://localhost:8080/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/github
```

## 테스트

```bash
./gradlew test
```

## 관련 저장소

- [agenthub-frontend](https://github.com/finalyongoh/agenthub-frontend) — Vue 3 웹 클라이언트, 화면 캡처
- [agenttrace](https://github.com/finalyongoh/agenttrace) — LangGraph 기반 저장소 분석 에이전트
- [docs](https://github.com/finalyongoh/docs) — 설계 산출물과 문서 리뷰 하네스
