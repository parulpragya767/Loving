#!/bin/sh

# Exit on error
set -e

# Substitute environment variables in the template
envsubst < /etc/prometheus/prometheus.yml.tpl > /etc/prometheus/prometheus.yml

# Execute the main prometheus command
exec /bin/prometheus \
  --config.file=/etc/prometheus/prometheus.yml \
  --enable-feature=agent \
  --storage.agent.path=/prometheus
