# ELK (sprint 12)

Компоненты: Elasticsearch, Logstash, Kibana.

- **Logstash input:** TCP `5044`, JSON lines (Spring Logstash encoder).
- **Pipeline:** `config/logstash/pipeline.conf` — фильтры маскировки чувствительных данных.
- **K8s:** Helm-сабчарт в umbrella `helm/my-bank-app/`.
