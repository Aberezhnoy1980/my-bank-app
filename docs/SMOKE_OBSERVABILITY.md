# Observability smoke check (Sprint 12)

Краткий чек-лист для **локального Kubernetes** (Helm release `mybank`, namespace `mybank`). Предполагается, что приложение уже поднято и проходит **[SMOKE_CHECK_SECURE.md](SMOKE_CHECK_SECURE.md)** (режим **C**).

Бизнес-логика в спринте **не менялась**; проверяем tracing, metrics, logs и UI стека observability.

## Prerequisites

- Образы `mybank/*:latest` собраны после изменений в `observability-support/` (Micrometer, Logback JSON, tracing).
- `helm upgrade --install mybank helm/my-bank-app -n mybank`
- Поды **Ready** (JVM + ELK могут стартовать 2–3 мин):

```bash
kubectl -n mybank get pods
kubectl -n mybank wait --for=condition=ready pod -l app.kubernetes.io/instance=mybank --timeout=600s
```

## URLs (Ingress)

| Компонент | URL | Примечание |
| --------- | --- | ---------- |
| Zipkin UI | http://localhost/zipkin/ | Отдельный Ingress + Traefik `stripPrefix` |
| Prometheus | http://localhost/prometheus/ | UI; metrics API: `/prometheus/metrics` |
| Grafana | http://localhost/grafana/ | `admin` / `admin`; папка **MyBank** |
| Kibana | http://kibana.localhost/ | Отдельный host (не path на `localhost`) |
| Front (трейсы) | http://localhost/ | Действия в UI создают spans |

Добавьте в `/etc/hosts` при необходимости: `127.0.0.1 kibana.localhost`.

Port-forward (если Ingress недоступен):

```bash
kubectl -n mybank port-forward svc/mybank-zipkin 9411:9411
kubectl -n mybank port-forward svc/mybank-prometheus 9090:9090
kubectl -n mybank port-forward svc/mybank-grafana 3000:3000
kubectl -n mybank port-forward svc/mybank-elk-kibana 5601:5601
```

## 1. Helm chart tests

```bash
helm lint helm/my-bank-app
helm test mybank -n mybank
```

Ожидаются **6** suite, все **Succeeded**: postgres, keycloak, kafka, zipkin, prometheus, logstash.

Test-поды после успеха **удаляются** — `kubectl get pods | grep test` часто пустой; это нормально (exit code 1 у `grep` в zsh — не ошибка kubectl).

## 2. Zipkin (distributed tracing)

1. Откройте http://localhost/zipkin/
2. В UI выполните в приложении: логин → deposit или transfer.
3. В Zipkin найдите trace (service name: `gateway`, `front`, `accounts-service`, …).

Проверка health из кластера (как в Helm test):

```bash
kubectl -n mybank run curl-zipkin --rm -it --restart=Never --image=curlimages/curl:8.8.0 -- \
  curl -fsS http://mybank-zipkin:9411/health
```

## 3. Prometheus (metrics scrape)

1. http://localhost/prometheus/targets — targets **UP** для сервисов с Actuator Prometheus.
2. Пример запроса: `http_server_requests_seconds_count` или `jvm_memory_used_bytes`.

```bash
kubectl -n mybank run curl-prom --rm -it --restart=Never --image=curlimages/curl:8.8.0 -- \
  curl -fsS http://mybank-prometheus:9090/prometheus/-/healthy
```

Scrape path у приложений: `/actuator/prometheus` (в Prometheus config — job’ы на ClusterIP).

## 4. Grafana (dashboards + alerts)

1. http://localhost/grafana/ → логин `admin` / `admin`
2. **Dashboards** → папка **MyBank**: HTTP, JVM, business.
3. **Alerting** → правила (HTTP 5xx rate, cash withdraw failures).

Datasource Prometheus внутри кластера: `http://mybank-prometheus:9090/prometheus` (с `--web.route-prefix`).

## 5. ELK (JSON logs)

1. Профиль `logstash` на runtime-сервисах + `LOGSTASH_HOST=mybank-elk-logstash`.
2. Действие в UI (deposit) → в Kibana (**Discover**) появляются JSON-логи с полями `traceId`, `spanId`, `service`.
3. Pipeline Logstash маскирует PII (см. `elk/config/logstash/pipeline.conf`).

```bash
kubectl -n mybank run nc-logstash --rm -it --restart=Never --image=busybox:1.36 -- \
  nc -zv mybank-elk-logstash 5044
```

## 6. Business counters (optional)

Метрики (рост при ошибках домена):

| Metric | Когда растёт |
| ------ | ------------- |
| `mybank_cash_withdraw_failed_total` | неуспешный withdraw |
| `mybank_transfer_failed_total` | неуспешный transfer |
| `mybank_notification_delivery_failed_total` | ошибка доставки уведомления |

Проверка в Prometheus/Grafana после намеренной ошибки (например withdraw больше баланса).

## 7. Maven verify (CI parity)

Из корня репозитория:

```bash
mvn -q verify
```

## Common issues

| Симптом | Что проверить |
| ------- | ------------- |
| Kibana 404 на `localhost/kibana` | Используйте **http://kibana.localhost/** |
| Zipkin пустой | Действие в UI после старта; `ZIPKIN_ENDPOINT` в pod env |
| Prometheus targets DOWN | Pod Ready; путь scrape `/actuator/prometheus` |
| Нет логов в Kibana | `SPRING_PROFILES_ACTIVE` содержит `logstash`; Logstash Ready |
| `helm test` только 3 suite | `helm upgrade` после добавления `test-observability.yaml` |
