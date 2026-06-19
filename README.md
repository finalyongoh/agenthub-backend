# agenthub-backend

## Local admin user

신규 가입 사용자는 기본 `USER` 권한으로 생성된다. 로컬 개발에서 `/api/admin/**` API를 테스트하려면 DB에서 해당 계정을 `ADMIN`으로 승격한 뒤 다시 로그인한다.

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = '가입한이메일@example.com';
```

Docker PostgreSQL 접속 예시:

```bash
docker exec -it <postgres-container-name> psql -U agenthub_user -d agenthub
```

## Social login

Google/GitHub 소셜 로그인은 `oauth` 프로필을 켜고 각 OAuth 앱의 client 값을 환경변수로 전달한다. 로컬에서는 `.env.local`에 값을 채운 뒤 실행 스크립트를 사용한다.

```bash
cp .env.local.example .env.local
./scripts/run-oauth-local.sh
```

OAuth 앱에는 아래 callback URL을 등록한다.

```text
http://localhost:8080/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/github
```
