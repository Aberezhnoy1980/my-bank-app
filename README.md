# my-bank-app

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-202x-0A2540)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![OAuth2](https://img.shields.io/badge/OAuth2-JWT-4B32C3)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

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
- Spring Data JPA + Hibernate
- PostgreSQL
- OAuth2 (Authorization Code + Client Credentials)
- Docker / Docker Compose

## Status

Инициализация репозитория и базового каркаса документации.
Детальная архитектура и реализация идут в ветке `module_three_sprint_nine_branch` (итерация `9.1A`).

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

## Gateway routes (9.1A.1)

- `/api/accounts/**` -> `accounts-service`
- `/api/cash/**` -> `cash-service`
- `/api/transfers/**` -> `transfer-service`
- `/api/notifications/**` -> `notifications-service`
