# my-bank-app

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-202x-0A2540)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![OAuth2](https://img.shields.io/badge/OAuth2-JWT-4B32C3)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
[![CI](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml/badge.svg)](https://github.com/Aberezhnoy1980/my-bank-app/actions/workflows/ci.yml)

Учебный микросервисный проект банка в рамках **module 3 / sprint 9** (Yandex Practicum).

## Scope

Проект на старте включает:

- Front UI
- Gateway API
- Service Discovery
- Externalized Config
- Accounts, Cash, Transfer, Notifications services

## Stack (baseline)

- Java 21
- Spring Boot, Spring Cloud
- Spring Data JPA + Hibernate (целевой слой персистентности по курсу)
- PostgreSQL
- OAuth2 (Authorization Code + Client Credentials)
- Docker / Docker Compose

На текущем этапе доменные данные аккаунтов могут храниться в упрощённом виде (например, in-memory для быстрой интеграции сервисов); переход на JPA и PostgreSQL идёт по мере развития проекта.

## Ports (локально по умолчанию)

| Компонент | Порт |
| --------- | ---- |
| Front UI | 8080 |
| Gateway | 8081 |
| Config Server | 8888 |
| Eureka | 8761 |
| accounts-service | 8091 |
| cash-service | 8092 |
| transfer-service | 8093 |
| notifications-service | 8094 |

## Status

Инициализация репозитория и базового каркаса документации.
Детальная архитектура и реализация идут в рабочей ветке проекта.

## Run (local, dev)

1. Запусти `discovery-server`.
2. Запусти `config-server`.
3. Запусти `gateway`.
4. Запусти `front` и остальные сервисы (`accounts-service`, `cash-service`, `transfer-service`, `notifications-service`).

Проверка инфраструктуры:

- Eureka dashboard: `http://localhost:8761`
- Config Server health: `http://localhost:8888/actuator/health`
- Gateway health: `http://localhost:8081/actuator/health`

## Run (Docker Compose)

```bash
docker compose up --build
```

Полезные команды:

```bash
docker compose ps
docker compose logs -f gateway
docker compose down
```

## Gateway routes

- `/api/accounts/**` -> `accounts-service`
- `/api/cash/**` -> `cash-service`
- `/api/transfers/**` -> `transfer-service`
- `/api/notifications/**` -> `notifications-service`

Фронт открывается по адресу `http://localhost:8080` после запуска нужных сервисов (через Gateway ходят клиенты во все перечисленные API).

## Smoke check (через Gateway, порт 8081)

Перед проверкой должны быть запущены Eureka, Config Server, Gateway и целевые микросервисы.

Примеры (демо-аккаунты по умолчанию включают в том числе `demo.user` и `alice.user`):

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
