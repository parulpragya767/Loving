global:
  scrape_interval: 15s

scrape_configs:
  - job_name: "loving-backend"
    metrics_path: "/api/actuator/prometheus"
    static_configs:
      - targets: ["loving-backend:8080"]

remote_write:
  - url: "${GRAFANA_PROMETHEUS_URL}"
    basic_auth:
      username: "${GRAFANA_PROMETHEUS_USERNAME}"
      password: "${GRAFANA_PROMETHEUS_API_KEY}"