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
`/api` to `agenthub-backend:8080` through the private `agenthub_internal` network.
