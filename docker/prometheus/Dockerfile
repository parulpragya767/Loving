# Stage 1: Get Prometheus binary
FROM prom/prometheus:v2.49.1 as prom

# Stage 2: Lightweight Alpine wrapper
FROM alpine:3.19

RUN apk add --no-cache gettext ca-certificates

# Copy Prometheus binary from official image
COPY --from=prom /bin/prometheus /bin/prometheus

# Copy startup script
COPY startup.sh /usr/local/bin/startup.sh
RUN chmod +x /usr/local/bin/startup.sh

# Create required directories and set permissions
RUN mkdir -p /etc/prometheus /prometheus && \
    chown -R nobody:nobody /etc/prometheus /prometheus

USER nobody

ENTRYPOINT ["/usr/local/bin/startup.sh"]
