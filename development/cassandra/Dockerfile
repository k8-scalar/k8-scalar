FROM cassandra:2.2.16

ENV CASSANDRA_WRITE_REQUEST_TIMEOUT 20000
ENV CASSANDRA_READ_REQUEST_TIMEOUT  50000

# [cassandra] Configure cassandra timeouts
RUN sed -ie "s@write_request_timeout_in_ms: 2000@write_request_timeout_in_ms: ${CASSANDRA_WRITE_REQUEST_TIMEOUT}@g" /etc/cassandra/cassandra.yaml
RUN sed -ie "s@read_request_timeout_in_ms: 5000@read_request_timeout_in_ms: ${CASSANDRA_READ_REQUEST_TIMEOUT}@g" /etc/cassandra/cassandra.yaml

COPY ready-probe.sh .
