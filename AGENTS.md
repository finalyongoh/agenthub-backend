# AgentHub Backend 작업 지침

## 문서 기준

- 이 repository에서 제품/요구사항/설계 문서를 참조할 때는 현재 디렉토리의 `docs/`를 기준으로 삼지 않는다.
- 문서의 source of truth는 GitHub repository `finalyongoh/docs`이다.
- 문서 확인, 비교, 인용, 요약, 구현 근거 확인은 GitHub MCP를 사용해 `finalyongoh/docs`에서 조회한다.

## GitHub MCP 사용

- GitHub 관련 작업은 가능한 한 GitHub MCP/app 도구를 우선 사용한다.
- `finalyongoh/docs` 문서 조회가 필요하면 GitHub MCP로 repository contents, files, commits, PR/issue context를 확인한다.
- GitHub MCP/app 도구가 보이지 않으면 작업을 진행하기 전에 사용자에게 GitHub MCP 설정이 필요하다고 안내한다.
- GitHub MCP로 필요한 정보를 얻을 수 없을 때만 `gh` CLI 같은 로컬 대안을 사용하고, 그 이유를 짧게 남긴다.

## RTK 사용

- 일반 shell 명령을 실행할 때는 `/Users/wolyong/.codex/RTK.md` 지침을 따른다.
- shell 명령은 기본적으로 `rtk` prefix를 붙여 실행한다. 예: `rtk git status`, `rtk ./gradlew test`.
- RTK 상태 확인이 필요하면 `rtk --version`, `rtk gain`, `which rtk`를 사용한다.
- `rtk`가 보이지 않으면 작업을 진행하기 전에 사용자에게 RTK 설정이 필요하다고 안내한다.
- context-mode MCP의 `ctx_execute`, `ctx_batch_execute`처럼 sandbox 내부에서 처리용 명령을 실행하는 경우에는 해당 도구의 지침을 우선한다.

## 변경 원칙

- backend 코드 변경은 현재 repository 패턴을 따른다.
- 문서와 코드가 충돌하면 `finalyongoh/docs`를 우선 기준으로 삼고, 충돌 사실을 사용자에게 보고한다.
