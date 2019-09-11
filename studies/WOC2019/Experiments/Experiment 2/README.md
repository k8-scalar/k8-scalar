The Cassandra based application and the low priority application are co-located on a node.
The experiment consists of two separate tests. During the first one, the Cassandra
application and the low priority pod each have a CPU request of 500m. For the
second test, Cassandra has a CPU request of 1500m while the low priority pod
has a CPU request of only 10m. The experiment controller sends an increasing amount of
requests to the Cassandra application, starting at 100 requests per second up to
600 requests per second, increasing with 50 requests per run. Each run lasts for 600
seconds.