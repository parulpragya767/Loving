# Stage 1: Get Prometheus binary
FROM prom/prometheus:v2.49.1 AS prom

# Stage 2: Lightweight Alpine wrapper
FROM alpine:3.19

# Install envsubst
RUN apk add --no-cache gettext

# Copy Prometheus binary from official image
COPY --from=prom /bin/prometheus /bin/prometheus

# Copy files
COPY prometheus.yml.tpl /etc/prometheus/prometheus.yml.tpl
COPY entrypoint.sh /entrypoint.sh

# Permissions
RUN chmod +x /entrypoint.sh && \
    mkdir -p /prometheus && \
    chown -R nobody:nobody /prometheus /etc/prometheus

# Drop privileges
USER nobody

# Expose Prometheus UI port
EXPOSE 9090

ENTRYPOINT ["/entrypoint.sh"]
