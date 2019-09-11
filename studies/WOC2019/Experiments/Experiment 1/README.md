The Cassandra application is deployed
with a CPU and memory request of respectively 1500m and 2GiB. No limits are
set. In this experiment, the experiment controller sends an increasing amount of
requests to the Cassandra application, starting at 100 requests per second up to
600 requests per second, increasing with 50 requests per run. Each run lasts for 600
seconds.