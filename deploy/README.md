# AgentHub Home Server Deployment

Target server:

- Deploy path: `/home/wolyong/deploy/agenthub`
- Public reverse proxy: existing `nginx_proxy`
- Shared Docker network: `web_network`
- App domain: `agenthub.wolyong.cloud`

## Required GitHub Actions Secrets

```text
SERVER_HOST=wolyong.cloud
SERVER_PORT=22
SERVER_USER=wolyong
SERVER_PASSWORD=<server password>
DEPLOY_PATH=/home/wolyong/deploy/agenthub

POSTGRES_PASSWORD=<database password>
AGENTHUB_JWT_SECRET=<long random secret>
APP_GITHUB_TOKEN=<optional GitHub API token used by the app>
```

## First Server Setup

1. Add DNS: `agenthub.wolyong.cloud` -> home server public IP.
2. Copy the HTTP-only nginx block into `/home/wolyong/deploy/nginx.conf` inside the `http { ... }` block.
3. Reload nginx:

```bash
docker exec nginx_proxy nginx -t
docker exec nginx_proxy nginx -s reload
```

4. Issue the certificate:

```bash
sudo certbot certonly --webroot -w /var/www/certbot -d agenthub.wolyong.cloud
```

5. Replace the temporary HTTP-only block with `nginx-agenthub.conf`.
6. Reload nginx again.

## Runtime

AgentHub containers do not publish host ports. The existing `nginx_proxy` reaches
`agenthub-frontend:80` through `web_network`, and the frontend container proxies
`/api`, `/oauth2`, and `/login/oauth2` to `agenthub-backend:8080` through the
private `agenthub_internal` network.

## Production OAuth

Create production Google and GitHub OAuth clients with these callback URLs:

```text
https://agenthub.wolyong.cloud/login/oauth2/code/google
https://agenthub.wolyong.cloud/login/oauth2/code/github
```

Create `/home/wolyong/deploy/agenthub/agenthub-backend/deploy/.env` from
`agenthub-backend/deploy/.env.example` and fill in:

```env
AGENTHUB_FRONTEND_BASE_URL=https://agenthub.wolyong.cloud
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=
```

Never commit the populated `.env` file.

Deploy with the explicit environment file:

```bash
docker compose \
  --env-file agenthub-backend/deploy/.env \
  -f agenthub-backend/deploy/docker-compose.prod.yml \
  up -d --build
```
