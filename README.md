# my-bank-app

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-202x-0A2540)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![OAuth2](https://img.shields.io/badge/OAuth2-JWT-4B32C3)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
[![CI](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml)

Учебный микросервисный проект банка (**module 3 / sprint 9**, Yandex Practicum).

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
- **Безопасность:** OAuth2 (Authorization Code для браузера, Client Credentials между сервисами), Keycloak в профиле `secure`  
- **Контракты:** Spring Cloud Contract — producer `accounts-service`, consumers `cash-service` и `transfer-service` (проверка против stubs)  
- **Контейнеризация:** Docker, Docker Compose  

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

Дополнительно:

```bash
docker compose ps
docker compose logs -f gateway
docker compose down
```

## Маршруты Gateway

| Префикс | Целевой сервис |
| ------- | -------------- |
| `/api/accounts/**` | accounts-service |
| `/api/cash/**` | cash-service |
| `/api/transfers/**` | transfer-service |
| `/api/notifications/**` | notifications-service |

UI: `http://localhost:8080`. HTTP-клиенты приложений обращаются к API через Gateway на порту **8081**.

## OAuth2 и Keycloak (профиль `secure`)

По умолчанию **`app.security.enabled=false`**: JWT не валидируется — удобно для CI и локальной отладки без IdP.

Включение защиты по JWT от Keycloak:

1. Поднять Keycloak (сервис в `docker-compose.yml`, импорт realm из `docker/keycloak/mybank-realm.json`). Админ-консоль: `http://localhost:8090` (учётные данные администратора — переменные окружения в Compose).  
2. Запустить сервисы с профилем **`secure`** и единым issuer, например `KEYCLOAK_ISSUER_URI=http://localhost:8090/realms/mybank` (переменная окружения или `-Dspring.profiles.active=secure`).

Клиенты из импортируемого realm (секреты для неучебных сред заменить):

| Client ID | Назначение | Secret (значение по умолчанию в YAML) |
| --------- | ---------- | --------------------------------------- |
| `mybank-front` | браузерный логин (`authorization_code`) на Front | `front-secret-change-me` |
| `mybank-services` | `client_credentials` между микросервисами | `services-secret-change-me` |

Пользователи realm, согласованные с демо-данными: `demo.user` / `demo`, `alice.user` / `alice`.

Поток: пользователь входит через Front → Gateway проверяет JWT → downstream получает тот же Bearer при проксировании; **`preferred_username`** сопоставляется с username аккаунта. При активном **`secure`** примеры **`curl`** ниже требуют заголовок `Authorization: Bearer <access_token>` (токен получить через UI или endpoint token Keycloak).

Детальный пошаговый сценарий проверки контура **`secure`** (переменные окружения, получение токена, типичные ошибки Keycloak/Gateway/Front, контуры «Docker + хост» и «полный Compose»): **[docs/SMOKE_CHECK_SECURE.md](docs/SMOKE_CHECK_SECURE.md)**.

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
curl -s -X POST http://localhost:8081/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"eventType":"MANUAL_TEST","message":"hello"}'
```
