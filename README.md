# my-bank-app

![Java](https://img.shields.io/badge/Java-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Liquibase](https://img.shields.io/badge/Liquibase-migrations-2962FF)
![OAuth2](https://img.shields.io/badge/OAuth2-JWT-4B32C3)
![Keycloak](https://img.shields.io/badge/Keycloak-OIDC-5865F2)
[![CI](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml)

![Docker Compose](https://img.shields.io/badge/Docker%20Compose-sprint%209-2496ED)
![Kubernetes](https://img.shields.io/badge/Kubernetes-sprint%2010-326CE5)
![Helm](https://img.shields.io/badge/Helm-umbrella%20chart-0F1689)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-sprint%2011-231F20)
![Zipkin](https://img.shields.io/badge/Zipkin-tracing-FE6A00)
![Prometheus](https://img.shields.io/badge/Prometheus-metrics-E6522C)
![Grafana](https://img.shields.io/badge/Grafana-dashboards-F46800)
![ELK](https://img.shields.io/badge/ELK-logs-005571)
![Spring Cloud Gateway](https://img.shields.io/badge/Spring%20Cloud%20Gateway-API-6DB33F)
![Spring Cloud Contract](https://img.shields.io/badge/Spring%20Cloud%20Contract-4.1-0A2540)

![Eureka](https://img.shields.io/badge/Eureka-Compose%2Flocal-9CA3AF)
![Spring Cloud Config](https://img.shields.io/badge/Spring%20Cloud%20Config-Compose%2Flocal-9CA3AF)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-UI-005F0F)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162)

Учебный микросервисный проект банка (**module 3**, Yandex Practicum; **sprint 9** — Docker Compose / Eureka / Config Server; **sprint 10** — Kubernetes / Helm; **sprint 11** — Apache Kafka; **sprint 12** — observability: Zipkin, Prometheus, Grafana, ELK).

![Схема взаимодействия сервисов](./docs/img/Image.png)

## Scope

Состав системы:

- Front UI
- Gateway API
- Service Discovery (Eureka)
- Externalized Config (Spring Cloud Config)
- Доменные сервисы: Accounts, Cash, Transfer, Notifications

## Stack

- **Runtime:** Java 21  
- **Framework:** Spring Boot, Spring Cloud  
- **Данные:** PostgreSQL, одна БД `mybank`, изоляция по **schema per service** для модулей с персистентностью: `accounts`, `notifications`, `cash`, `transfer`; миграции **Liquibase**  
- **События:** Apache Kafka (топик `bank.notifications`, JSON `eventType` + `message`); producers — Accounts / Cash / Transfer, consumer — Notifications  
- **Безопасность:** OAuth2 (Authorization Code для браузера, Client Credentials для вызовов Accounts из Cash/Transfer), Keycloak в профиле `secure`  
- **Контракты:** Spring Cloud Contract — producer `accounts-service`, consumers `cash-service` и `transfer-service` (проверка против stubs)  
- **Отказоустойчивость:** Resilience4j Circuit Breaker + Retry на вызовах **Accounts** из **cash-service** / **transfer-service**  
- **Контейнеризация:** Docker, Docker Compose; **оркестрация (sprint 10):** Kubernetes, Helm (`helm/my-bank-app`)  
- **Observability (sprint 12):** Micrometer Tracing → Zipkin; Actuator + Prometheus; Grafana (дашборды/алерты); JSON-логи → Logstash → Elasticsearch → Kibana (модуль `observability-support/`, Helm subcharts `zipkin/`, `prometheus/`, `grafana/`, `elk/`)  
- **Externalized Config:** Spring Cloud Config Server; канонические YAML для приложений лежат в **`config-server/src/main/resources/config/`** — общий **`application.yml`** (Eureka, Actuator) и файлы **`{spring.application.name}.yml`**. В каждом модуле **`application-local.yml`** задаёт те же значения для автономного старта без Config Server; при доступном сервере конфигурации последний импорт в **`application.yml`** подмешивает определения поверх локальных. В Docker Compose для клиентов задаётся **`CONFIG_SERVER_HOST=config-server`** (хост Config Server вместо `localhost`).

Источник истины по балансу — **accounts-service**; **cash** и **transfer** хранят у себя записи аудита операций (без дублирования баланса).

## Порты (локально, значения по умолчанию)

| Компонент | Порт |
| --------- | ---- |
| Front UI | 8080 |
| Gateway | 8081 |
| Config Server | 8888 |
| Eureka | 8761 |
| Keycloak (опционально, профиль `secure`) | 8090 |
| accounts-service | 8091 |
| cash-service | 8092 |
| transfer-service | 8093 |
| notifications-service | 8094 |

## Текущее состояние

Реализованы микросервисы и Gateway, профиль **`secure`** с Keycloak/JWT, персистентность для перечисленных schema, контрактные тесты Cash/Transfer → Accounts (Spring Cloud Contract).

**Sprint 10:** деплой в локальный Kubernetes (Rancher Desktop / k3s) через umbrella-чарт Helm; runtime-конфигурация в ConfigMap/Secret (Eureka и Config Server **не** входят в K8s-контур). Подробности — раздел [Kubernetes (Helm)](#kubernetes-helm) и **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)** (режим C).

**Sprint 11:** уведомления через **Kafka** (не REST); в кластере — сабчарт Bitnami Kafka (`helm/my-bank-app/charts/kafka`, KRaft, PVC). Перед первым `helm upgrade` один раз подтянуть зависимости Bitnami: `cd helm/my-bank-app/charts/kafka && helm dependency update` (нужен доступ к `oci://registry-1.docker.io/bitnamicharts`).

**Sprint 12:** observability в K8s — Zipkin, Prometheus, Grafana, ELK; Ingress для UI (`/zipkin`, `/prometheus`, `/grafana`; Kibana на **`http://kibana.localhost/`**). Чек-лист: **[docs/SMOKE_OBSERVABILITY.md](docs/SMOKE_OBSERVABILITY.md)**.

## Запуск без Docker (локальная разработка)

Для **`accounts-service`**, **`notifications-service`**, **`cash-service`** и **`transfer-service`** нужен доступный PostgreSQL (например контейнер из `docker compose up -d postgres`). Строка по умолчанию: `jdbc:postgresql://localhost:5432/mybank`, учётные данные `mybank` / `mybank` (как в Compose).

Последовательность запуска JVM-процессов:

1. `discovery-server`  
2. `config-server`  
3. `gateway`  
4. `front`, затем `accounts-service`, `cash-service`, `transfer-service`, `notifications-service` (порядок микросервисов после Gateway допускается гибкий при условии регистрации в Eureka).

Проверка доступности инфраструктуры:

- Eureka: `http://localhost:8761`  
- Config Server: `http://localhost:8888/actuator/health`  
- Gateway: `http://localhost:8081/actuator/health`  

## Docker Compose

```bash
docker compose up --build
```

Запуск полного secure-контура (все сервисы в Docker + JWT):

```bash
docker compose -f docker-compose.yml -f docker-compose.secure.yml up --build
```

Дополнительно:

```bash
docker compose ps
docker compose logs -f gateway
docker compose down
```

Альтернатива через `Makefile`:

```bash
make up
make up-secure
make down
make down-secure
```

**Полный стек в Docker с JWT** (`SPRING_PROFILES_ACTIVE=secure`): поверх **`docker-compose.yml`** подключается **`docker-compose.secure.yml`** — переменные Spring Security, **healthcheck** Keycloak, **`depends_on: service_healthy`**. Быстрая проверка API: **`make smoke-c`**. Переменные для профиля **`secure`**: **[`.env.example`](.env.example)**; чек-лист контуров A/B/C — **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)**.

## Kubernetes (Helm)

Целевой контур: **Ingress** на хосте `localhost` → Front (`/`), Gateway (`/api`), Keycloak (`/realms`, `/admin`); один PostgreSQL (StatefulSet); маршруты Gateway на ClusterIP-сервисы (`http://mybank-<service>:port`).

**Требования:** локальный кластер (например **Rancher Desktop** с включённым Kubernetes), `kubectl`, `helm`, собранные образы `mybank/*:latest`.

### 1. Сборка образов

Из корня репозитория (в **zsh** не используйте `$module` в теге — подставляйте имя модуля явно):

```bash
for module in gateway front accounts-service cash-service transfer-service notifications-service; do
  docker build -f docker/Dockerfile.module --build-arg MODULE="${module}" -t "mybank/${module}:latest" .
done
```

### 2. Установка release

```bash
kubectl create namespace mybank --dry-run=client -o yaml | kubectl apply -f -
helm upgrade --install mybank helm/my-bank-app -n mybank
```

Если ранее отключали subcharts через `--set keycloak.enabled=false` и т.п., сбросьте сохранённые values:

```bash
helm upgrade --install mybank helm/my-bank-app -n mybank --reset-values
```

Дождитесь **Running** (JVM на локальном k8s стартует 2–3 минуты):

```bash
kubectl -n mybank get pods
kubectl -n mybank wait --for=condition=ready pod --all --timeout=600s
```

### 3. URL и smoke

| Назначение | URL |
| ---------- | --- |
| Front UI | http://localhost/ (type **`http://`**, not bare `localhost` — avoids HSTS/https cookie mismatch) |
| API (через Ingress) | http://localhost/api/... |
| Keycloak OIDC | http://localhost/realms/mybank |
| Keycloak Admin | http://localhost/admin |
| Zipkin | http://localhost/zipkin/ |
| Prometheus | http://localhost/prometheus/ |
| Grafana | http://localhost/grafana/ (`admin` / `admin`) |
| Kibana | http://kibana.localhost/ |

Логин UI: `demo.user` / `demo`. API с токеном и типичные проблемы K8s — **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)** (режим **C**). Observability — **[docs/SMOKE_OBSERVABILITY.md](docs/SMOKE_OBSERVABILITY.md)**.

Проверка чарта (`helm test` — Postgres, Keycloak, Kafka, Zipkin, Prometheus, Logstash):

```bash
helm lint helm/my-bank-app
helm test mybank -n mybank
```

Полная переустановка (образы Docker **остаются** локально; удаляются release и данные в namespace, включая PVC Postgres при `delete namespace`):

```bash
helm uninstall mybank -n mybank
kubectl delete namespace mybank
# затем снова create namespace + helm upgrade --install ...
```

## Маршруты Gateway

| Префикс | Целевой сервис |
| ------- | -------------- |
| `/api/accounts/**` | accounts-service |
| `/api/cash/**` | cash-service |
| `/api/transfers/**` | transfer-service |

Уведомления **не** проксируются через Gateway: события публикуются в Kafka (`bank.notifications`), **notifications-service** потребляет их асинхронно. HTTP у Notifications — только **Actuator** (`/actuator/health`) для probes.

UI: **Docker Compose** — `http://localhost:8080`; **Kubernetes (Ingress)** — `http://localhost/`. HTTP-клиенты к API: Compose — Gateway **8081**; K8s — `http://localhost/api/...` через Ingress.

## OAuth2 и Keycloak (профиль `secure`)

По умолчанию **`app.security.enabled=false`**: JWT не валидируется — удобно для CI и локальной отладки без IdP.

Включение защиты по JWT от Keycloak:

1. Поднять Keycloak (сервис в `docker-compose.yml`, импорт realm из `docker/keycloak/mybank-realm.json`). Админ-консоль: `http://localhost:8090` (учётные данные администратора — переменные окружения в Compose).  
2. Запустить сервисы с профилем **`secure`**. Скопировать **[`.env.example`](.env.example)** → `.env`, экспортировать нужные переменные (или `docker compose --env-file .env`). Полный Compose: **`KC_HOSTNAME`** + **`KC_HOSTNAME_BACKCHANNEL_DYNAMIC`** в **`docker-compose.yml`**; остальное — **`docker-compose.secure.yml`**.

Клиенты из импортируемого realm (секреты для неучебных сред заменить):

| Client ID | Назначение | Secret (значение по умолчанию в YAML) |
| --------- | ---------- | --------------------------------------- |
| `mybank-front` | браузерный логин (`authorization_code`) на Front | `front-secret-change-me` |
| `mybank-services` | `client_credentials` (Cash/Transfer → Accounts через Gateway) | `services-secret-change-me` |

Пользователи realm, согласованные с демо-данными: `demo.user` / `demo`, `alice.user` / `alice`.

Поток: пользователь входит через Front → Gateway проверяет JWT → downstream получает тот же Bearer при проксировании; **`preferred_username`** сопоставляется с username аккаунта. При активном **`secure`** примеры **`curl`** ниже требуют заголовок `Authorization: Bearer <access_token>` (токен получить через UI или endpoint token Keycloak).

Проверка контура **`secure`**: **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)** (краткий чек-лист).

## Быстрая проверка API через Gateway (порт 8081)

Условие: запущены Eureka, Config Server, Gateway и нужные микросервисы.

Примеры без профиля **`secure`** (демо-аккаунты включают `demo.user`, `alice.user`):

```bash
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8081/api/accounts/me
curl -s -X PUT http://localhost:8081/api/accounts/me \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Demo User","birthDate":"1995-05-20"}'
curl -s -X POST http://localhost:8081/api/cash/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":100.50}'
curl -s -X POST http://localhost:8081/api/transfers \
  -H "Content-Type: application/json" \
  -d '{"recipientUsername":"alice.user","amount":50.00}'
```

После deposit/transfer проверьте логи **notifications-service** или таблицу `notifications.notification_event` — событие должно появиться через Kafka (в K8s — см. smoke в **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)**).
