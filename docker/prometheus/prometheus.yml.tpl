global:
  scrape_interval: 30s
  evaluation_interval: 30s

scrape_configs:
  - job_name: "loving-backend"
    metrics_path: "/api/actuator/prometheus"
    static_configs:
      - targets: ["${BACKEND_HOST}"]

remote_write:
  - url: "${GRAFANA_PROMETHEUS_URL}"
    basic_auth:
      username: "${GRAFANA_PROMETHEUS_USERNAME}"
      password: "${GRAFANA_PROMETHEUS_API_KEY}"

    queue_config:
      max_samples_per_send: 1000
      batch_send_deadline: 5s
      min_shards: 1
      max_shards: 2
      capacity: 2500

    metadata_config:
      send: false