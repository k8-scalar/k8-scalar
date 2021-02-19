FROM gcr.io/google_containers/heapster-grafana-amd64:v4.0.2

# [grafana] Configure theme to light
RUN sed -ie "s@;default_theme = dark@default_theme = light@g" /etc/grafana/grafana.ini
