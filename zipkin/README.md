# Zipkin (sprint 12)

Конфигурация развёртывания Zipkin для my-bank-app.

- **K8s:** Helm-сабчарт, подключается из `helm/my-bank-app/`.
- **UI:** после `helm upgrade` — Ingress `http://localhost/zipkin` (или port-forward на сервис `{release}-zipkin:9411`).
- **Приложения:** Micrometer Tracing → `http://{release}-zipkin:9411/api/v2/spans` (настраивается в коммитах приложений).
