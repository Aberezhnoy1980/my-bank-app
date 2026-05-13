# Проверка контура `secure` (Keycloak, JWT)

Документ описывает ручную проверку связки **OAuth2 / JWT**, **Gateway**, микросервисов и **Front** при активном профиле Spring **`secure`**. Для открытого API без JWT см. раздел «режимы» ниже и основной **README.md**.

---

## 1. Два режима работы

| Режим | Профиль Spring | Поведение |
| ----- | -------------- | --------- |
| Открытый API | профиль **`secure`** не активен, `app.security.enabled=false` | REST без обязательного JWT; подходит для CI и быстрых интеграционных проверок. |
| Защищённый контур | **`SPRING_PROFILES_ACTIVE=secure`** | Gateway и сервисы валидируют JWT от Keycloak; Front использует OAuth2 Login и передаёт Bearer на Gateway из серверного кода. |

Ниже — сценарии именно для **защищённого контура**.

---

## 2. Keycloak: realm, пользователи, типичные ошибки

- Realm импортируется из **`docker/keycloak/mybank-realm.json`** при старте Keycloak с **`--import-realm`**.  
- Пользователи **`demo.user`** и **`alice.user`** задаются этим файлом; при необходимости — доработка в админ-консоли.

### 2.1. Ошибка `invalid_grant`: «Account is not fully set up»

В новых версиях Keycloak учётная запись может считаться неполной (нет **email**, имени/фамилии, висят **Required actions**). Grant **`password`** не завершает профиль через UI, ответ может не содержать **`access_token`**.

**Устранение:**

- В админке: realm **`mybank`** → **Users** → пользователь → заполнить **Email**, **First name**, **Last name**, снять лишние **Required user actions**, сохранить.  
- В **`mybank-realm.json`** для импортируемых пользователей заданы поля профиля и **`"requiredActions": []`**, чтобы новые окружения не воспроизводили ту же ошибку.

### 2.2. Клиенты realm

| Client ID | Назначение |
| --------- | ---------- |
| `mybank-front` | браузерный поток **authorization_code** на Front; для отладки допускается **password grant** в `curl`. |
| `mybank-services` | **client_credentials** между сервисами. |

Секреты по умолчанию совпадают с **`application.yml`**; вне учебного стенда — заменить.

### 2.3. Время жизни access token

В учебном realm часто задан **`accessTokenLifespan`** порядка **300 с** (~5 мин). Импликации:

- команды **`curl`** с переменной **`TOKEN`** повторять после истечения срока;  
- в браузере обновление обычно выполняет **OAuth2AuthorizedClientManager** (refresh), при корректной конфигурации клиента и сессии — см. раздел 9.

### 2.4. Scope `offline_access`, refresh и ошибка `[not_allowed] Offline tokens not allowed`

**Зачем в приложении.** В конфигурации Front для профиля **`secure`** в запрос к Keycloak включён scope **`offline_access`** (вместе с **`openid`**, **`profile`**). Это стандартный OIDC-scope: IdP может выдать **refresh token**, чтобы обновлять **access token** без повторного ввода логина/пароля, пока живут политики сессии. Это не «режим без сети» и не ослабление шифрования — это согласование **жизненного цикла токенов** между Spring OAuth2 и Keycloak.

**Если в логах Front:** `ClientAuthorizationRequiredException` / долго не обновляется сессия — часто не хватает согласованности Keycloak с этим scope (см. ниже).

**Ошибка Keycloak:** `[not_allowed] Offline tokens not allowed for the user or client` после ввода логина.

1. **Клиент `mybank-front`.** Нужно, чтобы scope **`offline_access`** был **назначен клиенту** (в админке: **Clients** → **`mybank-front`** → вкладка **Client scopes** — блоки *Default* / *Optional*).  
   Не путать с разделом realm **Client scopes** (справочник всех scope’ов): там scope уже есть в таблице, но это **не** назначение клиенту.  
   Если у клиента на вкладке **Client scopes** кнопка **Add client scope** показывает *«There are no client scopes left to add»*, значит все доступные scope’ы realm уже привязаны к **`mybank-front`** — в том числе **`offline_access`**; добавлять нечего, проблема не в «пустом списке».

2. **Пользователь.** Часто нужно выдать пользователю realm-роль **`offline_access`**: **Users** → например **`demo.user`** → **Role mapping** → **Assign role** → **`offline_access`**. То же при необходимости для **`alice.user`**. Иначе Keycloak может отказать в offline refresh при запросе этого scope.

3. **Импорт realm.** В репозитории в **`docker/keycloak/mybank-realm.json`** у клиента **`mybank-front`** заданы **`defaultClientScopes`** / **`optionalClientScopes`** (вместе с **`offline_access`**); на **уже существующий** volume Keycloak полный JSON может не перезаписать клиента — тогда правки делаются в админке один раз или поднимается чистый volume при необходимости. Не задавайте в JSON только **`optionalClientScopes`: [`offline_access`]**: при импорте так можно «перебить» стандартный набор и получить **`[invalid_scope] Invalid scopes: openid profile offline_access`** в Docker — клиент перестаёт быть согласован с встроенными scope’ами realm.

**Ошибка `[invalid_scope] Invalid scopes: openid profile offline_access`** (часто полный Docker Compose): конфигурация клиента в Keycloak не содержит нужных **Default / Optional** scope’ов после импорта или устаревшего volume. Исправление: обновить **`mybank-realm.json`** (актуальный файл в репозитории), затем либо пересоздать volume Keycloak и поднять заново, либо в админке у **`mybank-front`** на вкладке **Client scopes** вручную привести списки к стандартным (как у нового OIDC-клиента) и добавить **`offline_access`** в Optional.

**После изменений в Keycloak:** выйти из приложения, при необходимости очистить cookies для **`localhost:8080`** / **`localhost:8090`**, войти снова (или проверить сценарий после истечения access token — раздел 9).

### 2.5. Front в Docker: стабильный OIDC без «двух issuer» (`[invalid_id_token]`, «Invalid token issuer»)

В Compose у Keycloak заданы **`KC_HOSTNAME=http://localhost:8090`** и **`KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true`** (см. **`docker-compose.yml`**), поэтому в **access / ID token** поле **`iss`** остаётся **`http://localhost:8090/realms/mybank`**, а запросы **refresh_token** на token endpoint по **внутреннему** URL (**`http://keycloak:8080/...`**) не требуют совпадения **`iss`** с hostname из URL (иначе Keycloak ≥23 возвращает **`[invalid_grant] Invalid token issuer`**).

При этом HTTP к Keycloak из контейнера **Front** для **token / jwk** в полном Docker идёт на **`http://keycloak:8080/...`** (сеть Compose). Если включить **`spring.security.oauth2.client.provider.keycloak.issuer-uri`** с URL **`http://keycloak:8080/realms/mybank`**, Spring выполняет **OIDC discovery** по этому адресу: в ответе часто оказывается **`issuer`** с хостом **`keycloak:8080`**, а не **`localhost:8090`**. Тогда проверка **id_token** не совпадает с **`iss`** в JWT → **`[invalid_id_token]`** и похожие ошибки при логине.

**Стабильная схема в репозитории:** для модуля **Front** при профиле **`secure`** регистрация OAuth2 собирается в коде — **`FrontKeycloakClientRegistrationConfiguration`**:

- ожидаемый OIDC issuer (**`issuerUri`**) задаётся переменной **`KEYCLOAK_OIDC_ISSUER_URI`** (по умолчанию **`http://localhost:8090/realms/mybank`**) — **только** для согласования с claim **`iss`**, без HTTP discovery по этому URL из контейнера;
- **`KEYCLOAK_TOKEN_URI`** и **`KEYCLOAK_JWK_SET_URI`** — **`http://keycloak:8080/...`** в Docker (после включения **`KC_HOSTNAME_BACKCHANNEL_DYNAMIC`** на Keycloak);
- **`KEYCLOAK_AUTHORIZATION_URI`** — URL для браузера (**`http://localhost:8090/...`**).

В **`application-local.yml` / `config-server/.../front.yml`** у провайдера **Keycloak для Front** поле **`issuer-uri` не задаётся** (чтобы Boot не подмешивал discovery поверх ручной регистрации). Класс **`OAuth2ClientRegistrationRepositoryConfiguration`** подключается через **`@Import`** внутри **`OAuth2ClientAutoConfiguration`**, поэтому **`spring.autoconfigure.exclude`** на него **не действует**. В модуле **Front** в **`FrontApplication`** задано **`@SpringBootApplication(exclude = OAuth2ClientAutoConfiguration.class)`**; сервис и репозиторий авторизованного клиента для **`secure`** — в **`FrontOAuth2LoginSupportConfiguration`**, регистрация клиента — в **`FrontKeycloakClientRegistrationConfiguration`**.

**На хосте (IDE / `mvn`)** значения по умолчанию совпадают: и issuer, и token/jwk — **`localhost:8090`**.

---

## 3. Multi-module Maven: зависимость модулей от `security-support`

Модули ссылаются друг на друга (например **`gateway`** → **`security-support`**). Пока артефакт **`security-support`** не установлен в локальный **`~/.m2`**, команда вида **`mvn -pl gateway spring-boot:run`** может завершиться ошибкой **«Could not find artifact … security-support»**.

**Вариант A — один раз из корня репозитория:**

```bash
mvn install -DskipTests
```

**Вариант B — при каждом запуске одного модуля подтягивать зависимости реактора:**

```bash
mvn -pl gateway -am spring-boot:run
```

Флаг **`-am`** включает сборку зависимых модулей в том же reactor-build.

---

## 4. Переменные окружения для профиля `secure`

Общие для JVM-процессов на хосте в одном сценарии:

```bash
export SPRING_PROFILES_ACTIVE=secure
export KEYCLOAK_ISSUER_URI=http://localhost:8090/realms/mybank
```

При смене секретов в Keycloak:

```bash
export KEYCLOAK_FRONT_SECRET=...
export KEYCLOAK_SERVICE_SECRET=...
```

**Замечание:** переменные, заданные через **`export` в текущей оболочке**, не попадают в уже запущенные контейнеры Docker. Для контейнеров — секция **`environment`** в Compose, **`--env-file`** или явная передача при **`docker run`**.

---

## 5. Два контура развёртывания

### A. Инфраструктура в Docker, приложения на хосте

Подходит для отладки из IDE / **`mvn`**: один issuer **`http://localhost:8090/realms/mybank`** и для браузера, и для JWKS.

Минимальный набор в Docker:

```bash
docker compose up -d keycloak discovery-server config-server
```

При занятости портов (**8080–8094**, **8761**, **8888** и т.д.) — остановить предыдущий стек:

```bash
docker compose down
```

Далее на хосте: **gateway** → микросервисы → **front**, с переменными из раздела 4.

### B. Полный стек в Docker Compose

Профиль **`secure`** и секреты задаются в **`environment`** сервисов в Compose (или через файл окружения для подстановки в compose). Для вызовов **между контейнерами** базовый URL Gateway — **`http://gateway:8081`**, а не **`http://localhost:8081`**: внутри контейнера **`localhost`** указывает на сам контейнер.

Рекомендуемый запуск:

```bash
docker compose -f docker-compose.yml -f docker-compose.secure.yml up --build -d
```

Где **`docker-compose.secure.yml`** включает профиль `secure` и параметры Keycloak для `front`, `gateway` и backend-сервисов.

Для быстрого повтора можно использовать `Makefile`:

```bash
make up-secure
make smoke-c
make down-secure
```

---

## 6. Получение токена для `curl` (password grant)

После того как пользователь в Keycloak имеет полный профиль (раздел 2.1):

```bash
curl -s -X POST "http://localhost:8090/realms/mybank/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=mybank-front" \
  -d "client_secret=front-secret-change-me" \
  -d "username=demo.user" \
  -d "password=demo"
```

В успешном ответе присутствует **`access_token`**.

Запись в переменную окружения (пример с временным файлом и Python):

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

**Диагностика:** если **`echo ${#TOKEN}`** возвращает **4**, в переменной часто оказывается строка **`null`** (отсутствие поля в JSON) — просмотреть сырой ответ Keycloak.

---

## 7. Запросы через Gateway (порт 8081) с JWT

Перед проверкой: Gateway и целевые сервисы запущены, в Eureka статус **UP**.

```bash
curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/actuator/health \
  -H "Authorization: Bearer $TOKEN"

curl -s -w "\nHTTP:%{http_code}\n" http://localhost:8081/api/accounts/me \
  -H "Authorization: Bearer $TOKEN"
```

Далее по необходимости: **PUT** `/api/accounts/me`, **POST** `/api/cash/deposit`, **POST** `/api/transfers`, **POST** `/api/notifications` — с заголовками **`Content-Type`** и **`Authorization: Bearer $TOKEN`**.

Ожидаемый результат: **HTTP 200** и непустое JSON-тело при корректных данных.

---

## 8. Браузер и Front (`http://localhost:8080`)

### 8.1. Поток OAuth2

Точка входа приложения — **`http://localhost:8080`**, не корень Keycloak. После логина редирект на OIDC содержит **`/realms/mybank/`** и **`client_id=mybank-front`**.

URL с **`/realms/master/`** и **`security-admin-console`** относятся к **админской консоли Keycloak**, не к логину приложения.

### 8.2. Сообщение «Accounts service is unavailable» при работающем `curl`

Цепочка причин:

1. Текст в UI формируется при исключении в **`AccountsGatewayClient.getCurrentAccount()`** (часто **401** от Gateway).  
2. Ручной **`curl`** передаёт **`Authorization: Bearer`** явно.  
3. Front вызывает Gateway через **`RestClient`**; при OAuth2 Login Bearer должен подставлять **`OAuth2LoginAccessTokenInterceptor`**.  
4. При использовании инжектированного **`RestClient.Builder`** в связке **Spring Cloud LoadBalancer** пост-процессоры могли изменять пайплайн так, что Bearer **не попадал** в итоговый запрос → **401** на Gateway при успешном **`curl`** с тем же токеном.

**Изменение в коде:** отдельный бин **`gatewayRestClient`**, собранный из «чистого» **`RestClient.builder()`** с явным добавлением OAuth2-interceptor — см. **`FrontGatewayRestClientConfiguration`** и **`@Qualifier("gatewayRestClient")`** в клиентах Gateway на стороне Front.

При активном **`secure`** и успешном логине профиль на странице должен подтягиваться без заглушки об ошибке.

### 8.3. Профиль `secure` на Front

В логах при старте должно отображаться активное профилирование, например:

`The following 1 profile is active: "secure"`

Без **`secure`** Front остаётся в режиме **`permitAll()`**, OAuth2-токен к Gateway с сервера не подставляется — возможен **401** на backend-вызовах при включённом **`secure`** на Gateway.

### 8.4. Перезапуск Front и cookies

После остановки процесса (**Ctrl+C**) и повторного **`mvn spring-boot:run`** серверная сессия не переносится. При «залипании» состояния после рестарта — очистить cookies для **`localhost:8080`** или использовать режим инкогнито и повторить вход.

---

## 9. Проверка refresh token (интервал ~5 минут)

Цель: после истечения **access token** по времени (поле **`exp`** в JWT; в realm часто ~5 мин) UI продолжает работать без повторного ручного логина.

1. Войти через **`http://localhost:8080`**, убедиться в корректности профиля и операций.  
2. Ожидание дольше времени жизни access token (например **6+ минут**).  
3. Обновить страницу или выполнить действие на форме.

Если после истечения access token появляется **401** и редирект на логин — логи Front: ошибки **OAuth2** / refresh; при необходимости временно **`logging.level.org.springframework.security.oauth2=DEBUG`**.

---

## 10. Симптомы и возможные причины

| Симптом | Возможная причина |
| ------- | ----------------- |
| **`invalid_grant`**, аккаунт не настроен | Неполный профиль пользователя в Keycloak (раздел 2.1). |
| **`TOKEN` длины 4 / пустой** | В ответе нет **`access_token`**; просмотреть сырое JSON-тело ошибки. |
| **`Connection refused` на `localhost:8081`** из контейнера | Внутри Docker **`localhost`** — сам контейнер; использовать имя сервиса (**`gateway`**). |
| **`curl` 200, в браузере «Accounts unavailable»** | Bearer не доходил до Gateway из-за конфигурации **`RestClient`** + LoadBalancer; проверить **`secure`** на Front и сессию после рестарта (раздел 8). |
| Редирект на **`master`** / админку Keycloak | Открыт неверный URL (раздел 8.1). |
| После **`mvn install`** артефакт не находится | Запуск из корня; использовать **`-pl … -am`**. |
| **`[not_allowed] Offline tokens not allowed`**, логин через браузер падает | Scope **`offline_access`** в запросе Front; проверить клиент **`mybank-front`** (вкладка **Client scopes**) и роль **`offline_access`** у пользователя — раздел **2.4**. |
| **`ClientAuthorizationRequiredException`** после простоя в UI | Истёк access token и не удался refresh; после настройки **`offline_access`** см. раздел **9**; полная перезагрузка сессии — выход и очистка cookies. |
| **`[invalid_grant] Invalid token issuer`** при refresh (несовпадение **`iss`** и hostname token endpoint) | В **`docker-compose.yml`** у Keycloak: **`KC_HOSTNAME=http://localhost:8090`**, **`KC_HOSTNAME_BACKCHANNEL_DYNAMIC=true`**; **Front** — **`KEYCLOAK_TOKEN_URI`** на **`http://keycloak:8080/...`** — §2.5. |
| **`[invalid_scope] Invalid scopes: openid profile offline_access`** | Клиент **`mybank-front`** в Keycloak без полного набора Default/Optional scope’ов (часто после минимального импорта или старого volume). См. раздел **2.4**, **`docker/keycloak/mybank-realm.json`**, при необходимости пересоздать volume Keycloak. |
| **`Unable to resolve Configuration with the provided Issuer`** `http://localhost:8090/realms/mybank` при старте **Front** в Docker | Boot тянет **`OAuth2ClientRegistrationRepositoryConfiguration`** через **`@Import`** в **`OAuth2ClientAutoConfiguration`**; исключение только репозитория из YAML не помогает. См. §2.5 — **`FrontApplication`**: **`exclude = OAuth2ClientAutoConfiguration`**, ручная регистрация + **`FrontOAuth2LoginSupportConfiguration`**. |
| Старт **accounts / cash / transfer / notifications**: *The Issuer … did not match the requested issuer* (**localhost** vs **keycloak**) | У **`oauth2.client.provider.keycloak`** не использовать **`issuer-uri`** вместе с другим «желаемым» issuer в Compose; задать **`token-uri`** и **`jwk-set-uri`** (**`KEYCLOAK_TOKEN_URI`**, **`KEYCLOAK_JWK_SET_URI`** в **`docker-compose.secure.yml`**). **Resource Server** по-прежнему **`jwt.issuer-uri`** = публичный realm. |
| **`[invalid_id_token]`**, «invalid claims», **`iss=http://localhost:8090/realms/mybank`** | Discovery по **`keycloak:8080`** дал другой **`issuer`**, чем **`iss`** в JWT. См. раздел **2.5** — ручная регистрация Front, **`KEYCLOAK_OIDC_ISSUER_URI`**, без **`issuer-uri`** в YAML провайдера. |

---

## 11. Связанные артефакты в репозитории

| Файл / область | Назначение |
| -------------- | ---------- |
| `docker-compose.yml` | Keycloak, Eureka, Config Server, Gateway, сервисы, переменные для Docker. |
| `docker/keycloak/mybank-realm.json` | Realm, клиенты, пользователи для импорта. |
| `front/.../FrontGatewayRestClientConfiguration.java` | Явный **`RestClient`** для вызовов Gateway с Bearer. |
| `front/.../FrontApplication.java` | **`exclude = OAuth2ClientAutoConfiguration`**: отключение Boot OAuth2 client auto-config с неработающим **`@Import`** репозитория. |
| `front/.../FrontKeycloakClientRegistrationConfiguration.java` | Ручная **`ClientRegistration`** (issuer JWT vs token/jwk URL в Docker). |
| `front/.../FrontOAuth2LoginSupportConfiguration.java` | **`OAuth2AuthorizedClientService`** / **`OAuth2AuthorizedClientRepository`** вместо отключённого **`OAuth2WebSecurityConfiguration`**. |
| `front/.../FrontOAuth2AuthorizedClientConfiguration.java` | **`OAuth2AuthorizedClientManager`** с refresh и clock skew. |
| `front/.../OAuth2LoginAccessTokenInterceptor.java` | Подстановка access token из OAuth2-сессии. |
| `README.md` | Порты, запуск, ссылка на этот документ. |

---

## 12. Актуальность документа

Сценарии ориентированы на **Spring Boot 3.x**, Keycloak в dev-режиме, смешанный запуск Docker и хоста. При обновлении Spring Cloud или Keycloak отдельные симптомы могут отличаться — таблицу в разделе 10 имеет смысл дополнять по факту.
