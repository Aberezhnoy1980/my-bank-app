# Smoke check с профилем `secure` (Keycloak + JWT)

Этот документ — практическая памятка по проверке всего контура **OAuth2 / JWT + Gateway + микросервисы + Front** после серии реальных отладочных сессий. Его можно держать в архиве методики или ссылаться из README.

---

## 1. Зачем два режима и что мы проверяем

| Режим | Профиль Spring | Что проверяем |
|--------|----------------|---------------|
| **Открытый API** | профиль `secure` **не** активен, `app.security.enabled=false` | REST без JWT, удобно для CI и быстрых интеграционных проверок. |
| **Защищённый контур** | **`SPRING_PROFILES_ACTIVE=secure`** | Gateway и сервисы валидируют JWT от Keycloak; Front использует **OAuth2 Login** и передаёт Bearer на Gateway при вызовах из серверного кода. |

Smoke в этом документе — именно **защищённый контур**.

---

## 2. Артефакты и пользователи Keycloak

- Realm импортируется из **`docker/keycloak/mybank-realm.json`** при старте Keycloak с `--import-realm`.
- Пользователи **`demo.user`** и **`alice.user`** задаются этим файлом (и при необходимости дополняются в админке).

### 2.1. Ошибка `invalid_grant` — «Account is not fully set up»

В новых версиях Keycloak профиль пользователя может считаться неполным (нет **email**, **имени/фамилии**, висят **Required actions**). Grant **`password`** не может «довести» профиль через UI, поэтому Keycloak возвращает ошибку без `access_token`.

**Что сделать:**

- В админке Keycloak: realm **`mybank`** → **Users** → пользователь → заполнить **Email**, **First name**, **Last name**, снять лишние **Required user actions**, сохранить.
- В репозитории в JSON импорта добавлены поля профиля и `"requiredActions": []` для импортируемых пользователей (чтобы новые окружения не ловили ту же проблему).

### 2.2. Клиенты realm (для ориентира)

| Client ID | Назначение |
|-----------|------------|
| `mybank-front` | браузерный поток **authorization_code** на Front; для отладки можно использовать **password grant** в `curl`. |
| `mybank-services` | **client_credentials** между сервисами. |

Секреты по умолчанию совпадают с `application.yml` (в проде — заменить).

### 2.3. Срок жизни access token

В realm для учебного стенда часто задан **`accessTokenLifespan`** порядка **300 секунд (5 минут)**. Поэтому:

- **`curl`** с переменной **`TOKEN`** нужно повторять после истечения срока.
- В браузере обновление обычно делает **OAuth2AuthorizedClientManager** (refresh token), если сессия и клиент настроены корректно — см. раздел 9.

---

## 3. Multi-module Maven: почему нужен `install`

Модули зависят друг от друга (например, **`gateway`** → **`security-support`**). Пока артефакт **`security-support`** не установлен в локальный **`~/.m2`**, команда вида `mvn -pl gateway spring-boot:run` может упасть с **«Could not find artifact … security-support»**.

**Один раз из корня репозитория:**

```bash
mvn install -DskipTests
```

Либо при каждом запуске одного модуля:

```bash
mvn -pl gateway -am spring-boot:run
```

(`-am` — собрать зависимые модули реактора.)

---

## 4. Переменные окружения для профиля `secure`

Общие для всех JVM-процессов на хосте в одном сценарии:

```bash
export SPRING_PROFILES_ACTIVE=secure
export KEYCLOAK_ISSUER_URI=http://localhost:8090/realms/mybank
```

Опционально (если меняли секреты в Keycloak):

```bash
export KEYCLOAK_FRONT_SECRET=...
export KEYCLOAK_SERVICE_SECRET=...
```

**Важно:** `export` в терминале действует только на процессы, запущенные **из этого терминала после** экспорта. Уже запущенные **Docker-контейнеры** переменные из оболочки **не получают** — их нужно передавать через **`docker-compose`** / **`environment`** / **`--env-file`**.

---

## 5. Два контура запуска (кратко)

### Контур A — инфраструктура в Docker, приложения на хосте

Подходит для отладки с IDE/`mvn`: один issuer **`http://localhost:8090/realms/mybank`** и для браузера, и для JWKS.

**Минимально в Docker:**

```bash
docker compose up -d keycloak discovery-server config-server
```

Остановить полный стек перед этим, если заняты порты (**8080–8094**, **8081**, и т.д.):

```bash
docker compose down
```

Дальше на хосте в разумном порядке: **gateway** → микросервисы → **front**, везде с переменными из раздела 4.

### Контур B — всё в Docker Compose

Тот же профиль и секреты задаются в **`environment`** сервисов в Compose (или через файл окружения для подстановки в compose). Для вызовов **между контейнерами** базовые URL к Gateway задаются через имя сервиса (**`http://gateway:8081`**), а не **`http://localhost:8081`** — иначе внутри контейнера **`localhost`** указывает на сам контейнер.

Подробности конфигурации Compose см. в **`docker-compose.yml`** и комментариях в репозитории.

---

## 6. Получение токена для `curl` (password grant)

После того как пользователь в Keycloak «полный» (раздел 2.1):

```bash
curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo"
```

В ответе должен быть **`access_token`**.

Положить в переменную (пример с записью во временный файл и Python):

```bash
curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo" \
  -o /tmp/token.json

export TOKEN=$(python3 -c "import json; print(json.load(open('/tmp/token.json'))['access_token'])")
```

**Диагностика:** если **`echo ${#TOKEN}`** показывает **4**, в переменной часто оказывается строка **`null`** (от `jq` при отсутствии поля) — снова смотри **сырой JSON** ответа Keycloak.

---

## 7. Smoke через Gateway (порт 8081) с JWT

Убедись, что Gateway и нужные сервисы подняты и в Eureka в статусе **UP**.

Базовые проверки:

```bash
curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/actuator/health \
  -H "Authorization: Bearer $TOKEN"

curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/api/accounts/me \
  -H "Authorization: Bearer $TOKEN"
```

Дальше по домену (при необходимости): **PUT** `/api/accounts/me`, **POST** `/api/cash/deposit`, **POST** `/api/transfers`, **POST** `/api/notifications` — все с теми же заголовками **`Content-Type`** и **`Authorization: Bearer $TOKEN`**.

Ожидание: **HTTP 200** и осмысленное JSON-тело.

---

## 8. Браузер и Front (`http://localhost:8080`)

### 8.1. Нормальный поток OAuth2

Точка входа приложения — **`http://localhost:8080`**, не корень Keycloak. После логина редирект на OIDC выглядит как:

`/realms/mybank/protocol/openid-connect/auth?...client_id=mybank-front...`

URL с **`/realms/master/`** и **`security-admin-console`** — это **админская консоль Keycloak**, не логин приложения.

### 8.2. Почему «curl ок», а страница Front показывала «Accounts service is unavailable»

Краткая цепочка:

1. Сообщение в UI выводится, когда **`AccountsGatewayClient.getCurrentAccount()`** бросает исключение (часто **401** от Gateway).
2. **`curl`** сам передаёт **`Authorization: Bearer`**.
3. Front вызывает Gateway через **`RestClient`**; в режиме OAuth2 Login токен должен подставлять **`OAuth2LoginAccessTokenInterceptor`**.
4. На практике в связке **Spring Cloud Eureka + LoadBalancer** инжектированный **`RestClient.Builder`** дополнительно обрабатывался пост-процессорами, из-за чего **Bearer не попадал** в итоговый запрос → Gateway отвечал **401**, хотя ручной **`curl`** с токеном работал.

**Исправление в коде:** отдельный бин **`gatewayRestClient`**, собранный через «чистый» **`RestClient.builder()`** и явное добавление OAuth2-interceptor (см. **`FrontGatewayRestClientConfiguration`** и использование **`@Qualifier("gatewayRestClient")`** в gateway-клиентах Front).

После этого при активном профиле **`secure`** и успешном логине в Keycloak страница должна подтягивать профиль без заглушки об ошибке.

### 8.3. Профиль `secure` на Front

В логах при старте должно быть что-то вроде:

`The following 1 profile is active: "secure"`

Без **`secure`** Front остаётся в режиме **`permitAll()`**, OAuth2 к Gateway с сервера не подставляется, и снова возможен **401** на backend-вызовах при включённом secure на Gateway.

### 8.4. Перезапуск Front и cookies

После **`Ctrl+C`** и нового **`mvn spring-boot:run`** серверная сессия Tomcat не переносится. Имеет смысл очистить cookies для **`localhost:8080`** или зайти в **инкогнито** и пройти логин заново, если поведение после рестарта кажется «странным».

---

## 9. Проверка refresh token (после ~5 минут)

Убедиться, что **access token** истёк по времени (в JWT поле **`exp`**, в realm типично ~5 минут), но **UI продолжает работать** без повторного ручного логина:

1. Залогиниться через **`http://localhost:8080`**, убедиться, что профиль и операции ок.
2. Подождать **больше времени жизни access token** (например **6+ минут**).
3. Обновить страницу или выполнить действие (формы на главной).

Если после истечения **access token** всё ломается с **401** и редиректом на логин — смотреть логи Front на предмет ошибок **OAuth2** / refresh (при необходимости временно включить **`logging.level.org.springframework.security.oauth2=DEBUG`**).

---

## 10. Чеклист «всё пошло не так»

| Симптом | Возможная причина |
|---------|-------------------|
| **`invalid_grant`**, аккаунт не настроен | Неполный профиль пользователя в Keycloak (раздел 2.1). |
| **`TOKEN` длины 4 / пустой** | В ответе нет **`access_token`**; смотреть сырое JSON-тело ошибки. |
| **`Connection refused` на `localhost:8081`** из контейнера | Внутри Docker **`localhost`** — сам контейнер; для клиентов нужен хост сервиса (например **`gateway`**). |
| **`curl` 200, браузер «Accounts unavailable»** | Исторически: Bearer не доходил до Gateway из-за **`RestClient.Builder`** + LoadBalancer; после фикса — проверить **`secure`** на Front и сессию после рестарта. |
| Редирект на **`master`** / админку Keycloak | Открыт не тот URL (раздел 8.1). |
| После **`mvn install`** всё ещё нет артефакта | Запуск из корня; проверить **`-pl … -am`**. |

---

## 11. Связанные файлы в репозитории

| Файл / область | Назначение |
|----------------|------------|
| `docker-compose.yml` | Keycloak, Eureka, Config Server, Gateway, сервисы, переменные для Docker. |
| `docker/keycloak/mybank-realm.json` | Realm, клиенты, пользователи для импорта. |
| `front/.../FrontGatewayRestClientConfiguration.java` | Явный **`RestClient`** для вызовов Gateway с Bearer. |
| `front/.../OAuth2LoginAccessTokenInterceptor.java` | Подстановка access token из OAuth2-сессии. |
| `README.md` | Краткие порты, запуск, ссылка на этот документ. |

---

## 12. Версия документа

Сценарии и ошибки зафиксированы по результатам отладки учебного стенда (Spring Boot 3.x, Keycloak в dev-режиме, смешанный запуск Docker + хост). При обновлении версий Spring Cloud / Keycloak часть симптомов может отличаться — имеет смысл дополнять таблицу в разделе 10.
