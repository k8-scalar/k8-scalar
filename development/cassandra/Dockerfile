FROM gcr.io/google-samples/cassandra:v12

ENV CASSANDRA_WRITE_REQUEST_TIMEOUT 20000
ENV CASSANDRA_READ_REQUEST_TIMEOUT  50000

# [ubuntu] Download dependencies
RUN apt-get update && apt-get install -y python git 

# [cqlsh] Retrieve missing library: cqlshlib
RUN cd /root \
 && git clone https://github.com/apache/cassandra.git \
 && mv cassandra/pylib/cqlshlib /usr/local/lib/python2.7/site-packages/ \
 && rm -r cassandra

# [cqlsh] Configure cqlshlib path 
RUN sed -ie "s@cqlshlibdir = os.path.join(CASSANDRA_PATH, 'pylib')@cqlshlibdir = os.path.join(CASSANDRA_PATH, '/usr/local/lib/python2.7/site-packages/')@g" /usr/local/apache-cassandra-3.9/bin/cqlsh.py

# [cassandra] Configure cassandra timeouts
RUN sed -ie "s@write_request_timeout_in_ms: 2000@write_request_timeout_in_ms: ${CASSANDRA_WRITE_REQUEST_TIMEOUT}@g" /etc/cassandra/cassandra.yaml
RUN sed -ie "s@read_request_timeout_in_ms: 5000@read_request_timeout_in_ms: ${CASSANDRA_READ_REQUEST_TIMEOUT}@g" /etc/cassandra/cassandra.yaml
