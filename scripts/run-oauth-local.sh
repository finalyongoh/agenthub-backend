#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

if [[ -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
fi

required_vars=(
  GOOGLE_CLIENT_ID
  GOOGLE_CLIENT_SECRET
  GITHUB_CLIENT_ID
  GITHUB_CLIENT_SECRET
)

missing_vars=()
for var_name in "${required_vars[@]}"; do
  if [[ -z "${!var_name:-}" ]]; then
    missing_vars+=("${var_name}")
  fi
done

if (( ${#missing_vars[@]} > 0 )); then
  echo "Missing OAuth env values in ${ENV_FILE}: ${missing_vars[*]}" >&2
  echo "Fill them after creating Google/GitHub OAuth apps, then run this script again." >&2
  exit 1
fi

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-oauth}"
export AGENTHUB_FRONTEND_BASE_URL="${AGENTHUB_FRONTEND_BASE_URL:-http://localhost:5173}"

cd "${ROOT_DIR}"
exec ./gradlew bootRun
