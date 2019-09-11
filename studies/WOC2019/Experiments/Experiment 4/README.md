The Cassandra application is deployed without a low priority app present.
The Cassandra application has a CPU request of 1500m.
The HPA is configured to scale Cassandra when its CPU usage rises above 110% of its CPU request.
An empty node is made available for the HPA to scale a replica of the Cassandra application on.
An increasing amount of requests per second starting at 100
requests per second up to 600 requests per second, increasing with 50 requests per run, is applied to the application.
Each run lasts for 600 seconds