# Prometheus (sprint 12)

Конфигурация Prometheus: `config/prometheus.yml`, развёртывание через Helm из `helm/my-bank-app/`.

Scrape targets: `*/actuator/prometheus` для gateway, front и микросервисов (рендерится в Helm ConfigMap).
