:# Secure contour smoke check

Short checklist for **profile `secure`** (Keycloak + JWT). Open API without JWT: omit `SPRING_PROFILES_ACTIVE=secure`, keep `app.security.enabled=false` (default).

Variable names and defaults: **[`.env.example`](../.env.example)** at the repo root.

## Modes

| Mode | How to run |
| ---- | ---------- |
| **A — infra in Docker, apps on host** | `docker compose up -d keycloak discovery-server config-server postgres` → export vars from `.env.example` → start Gateway and services from IDE/`mvn` |
| **B — full stack in Docker** | `docker compose -f docker-compose.yml -f docker-compose.secure.yml up --build` or `make up-secure` |

Inside containers use service DNS (`http://gateway:8081`), not `localhost:8081`.

## Quick verification

1. **Eureka** — `http://localhost:8761` — services registered.
2. **API smoke (B)** — `make smoke-c` (password grant + Gateway with Bearer).
3. **UI** — `http://localhost:8080` — login `demo.user` / `demo`.

### Token for curl (password grant)

```bash
export TOKEN=$(curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo" | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/accounts/me
```

After code/config changes in Docker, rebuild: `docker compose ... up --build`.

## Common issues

| Symptom | What to check |
| ------- | ------------- |
| `invalid_grant` / account not set up | Keycloak user profile (email, name); see realm import `docker/keycloak/mybank-realm.json` |
| `Connection refused` on `localhost:8081` from a container | Use `http://gateway:8081` |
| Login redirect to `http://keycloak:8080` | Front OAuth2 URIs: browser → `localhost:8090`, token/JWKS → `keycloak:8080` in full Docker (see README) |
| `[invalid_grant] Invalid token issuer` on refresh | Keycloak `KC_HOSTNAME` + `KC_HOSTNAME_BACKCHANNEL_DYNAMIC` in `docker-compose.yml` |
| Services start before Keycloak | Wait for Keycloak healthcheck / `depends_on: service_healthy` in `docker-compose.secure.yml` |
| `export` in shell does not affect running containers | Set env in Compose or `--env-file` |

More detail (OAuth2 Front wiring, offline_access, Maven `-am`): internal project notes or course materials.
