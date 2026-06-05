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
