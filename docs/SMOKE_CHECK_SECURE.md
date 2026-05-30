# Secure contour smoke check

Short checklist for **profile `secure`** (Keycloak + JWT). Open API without JWT: omit `SPRING_PROFILES_ACTIVE=secure`, keep `app.security.enabled=false` (default).

Variable names and defaults: **[`.env.example`](../.env.example)** at the repo root.

## Modes

| Mode | How to run |
| ---- | ---------- |
| **A — infra in Docker, apps on host** | `docker compose up -d keycloak discovery-server config-server postgres` → export vars from `.env.example` → start Gateway and services from IDE/`mvn` |
| **B — full stack in Docker** | `docker compose -f docker-compose.yml -f docker-compose.secure.yml up --build` or `make up-secure` |
| **C — Kubernetes (Helm)** | Build `mybank/*` images → `helm upgrade --install mybank helm/my-bank-app -n mybank` → wait for pods → UI at `http://localhost/` (see [README](../README.md#kubernetes-helm)) |

Inside containers use service DNS (`http://mybank-gateway:8081`, `http://mybank-keycloak:8080`), not `localhost:8081` from a pod.

## Quick verification

### Compose (A / B)

1. **Eureka** — `http://localhost:8761` — services registered (mode A).
2. **API smoke (B)** — `make smoke-c` (password grant + Gateway with Bearer).
3. **UI** — `http://localhost:8080` — login `demo.user` / `demo`.

### Kubernetes (C)

1. **Pods** — `kubectl -n mybank get pods` — all **Running** / **Ready** (allow ~2–3 min after install; Kafka controller/broker may need extra time on first start).
2. **OIDC** — `curl -fsS http://localhost/realms/mybank/.well-known/openid-configuration | head -c 120` — HTTP 200.
3. **Helm test** — `helm test mybank -n mybank` — hooks for Postgres, Keycloak, Kafka, Zipkin (`/health`), Prometheus (`/prometheus/-/healthy`), Logstash (TCP `5044`). Run when observability pods are **Ready** (Logstash may need ~1–2 min after install).
4. **API smoke** — token + Gateway via Ingress (below).
5. **UI** — `http://localhost/` only (not `127.0.0.1`, not `:8080` unless port-forward) — login `demo.user` / `demo`; profile, deposit, withdraw, transfer to `alice.user`.
6. **Notifications via Kafka** — after deposit/transfer, logs: `kubectl -n mybank logs deploy/mybank-notifications-service --tail=50 | grep "Notification persisted"`; or DB: `kubectl -n mybank exec -it statefulset/mybank-postgres -- psql -U mybank -d mybank -c "SELECT id, event_type, message FROM notifications.notification_event ORDER BY id DESC LIMIT 5;"`.
7. **Refresh (optional)** — wait longer than access token TTL (realm default 300s), reload UI — session should refresh without Keycloak login form.

### Token for curl (password grant)

**Compose** (Keycloak on host port 8090):

```bash
export TOKEN=$(curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo" | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/accounts/me
```

**Kubernetes** (Keycloak behind Ingress on port 80):

```bash
export TOKEN=$(curl -s -X POST "http://localhost/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo" | jq -r .access_token)

curl -s -H "Authorization: Bearer $TOKEN" http://localhost/api/accounts/me
```

Expect JSON profile (not `401`). After code/config changes in Docker, rebuild: `docker compose ... up --build`. After Helm/Java changes, rebuild affected images and `helm upgrade` + rollout restart if needed.

```bash
helm lint helm/my-bank-app
helm test mybank -n mybank
```

## Common issues

| Symptom | What to check |
| ------- | ------------- |
| `invalid_grant` / account not set up | Keycloak user profile (email, name); see realm import `docker/keycloak/mybank-realm.json` or `helm/my-bank-app/charts/keycloak/realm/mybank-realm.json` |
| `Connection refused` on `localhost:8081` from a container | Use in-cluster DNS (`http://mybank-gateway:8081`) or Ingress `http://localhost/api/...` |
| Login redirect to `http://keycloak:8080` | Front OAuth2 URIs: browser → public host (`localhost`), token/JWKS → Keycloak Service in Docker/K8s (see README) |
| `[invalid_grant] Invalid token issuer` on refresh | Keycloak `KC_HOSTNAME` + `KC_HOSTNAME_BACKCHANNEL_DYNAMIC` in Compose; in K8s chart: `hostname: http://localhost` |
| Services start before Keycloak | Wait for Keycloak healthcheck / `depends_on: service_healthy` in Compose; in K8s wait for Keycloak pod Ready |
| `export` in shell does not affect running containers | Set env in Compose or `--env-file` |
| **K8s:** `401` on `/api/accounts/me` with valid token; UI «Accounts service is unavailable» | JWT **`issuer-uri`** is public (`http://localhost/realms/mybank`), but pods cannot load JWKS from that URL. Set **`spring.security.oauth2.resourceserver.jwt.jwk-set-uri`** to in-cluster Keycloak (`http://mybank-keycloak:8080/realms/mybank/protocol/openid-connect/certs`) on **gateway** and all JWT resource servers — see Helm ConfigMaps. Symptom: Gateway may authorize the request, downstream still returns 401. |
| **K8s:** `[authorization_request_not_found]` or redirect loop in browser | Open **`http://localhost/`** explicitly (typing `localhost` alone may use `https://` via HSTS — another cookie jar). Clear site data for localhost. Chart sets fixed `redirect-uri` and `forward-headers-strategy`. After a failed login, do not edit `/login?error` manually — log out or clear cookies and retry. |
| **K8s:** `ImagePullBackOff` | Build and tag `mybank/<module>:latest` locally (see README) |
| **K8s:** no notification rows after UI ops | Kafka pods not Ready; check `kubectl -n mybank get pods -l app.kubernetes.io/name=kafka`; producer logs in accounts/cash/transfer; topic `bank.notifications` (auto-create enabled in chart) |
| **Helm:** missing `kafka-*.tgz` | Run `helm dependency update` inside `helm/my-bank-app/charts/kafka` (OCI pull to Bitnami), then commit vendored chart |
| **K8s:** CrashLoop / probe failures right after install | JVM warm-up; chart uses higher `initialDelaySeconds` on readiness/liveness — wait or `kubectl logs` |
| **Helm:** white screen / no Keycloak on `/` | `helm upgrade --reset-values` if subcharts were previously disabled with `--set ...=false` |

More detail (OAuth2 Front wiring, offline_access, Maven `-am`): course materials or internal notes.
