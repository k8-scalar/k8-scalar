Two separate tests are run. The first one subjects
the SaaS application to a linearly increasing workload to see how much requests
one replica can handle. The SaaS application is configured so that it processes the
requests in a CPU intensive manner. Its CPU request is set to 1.5 CPU.
The linearly increasing workload applied to the SaaS application starts 
at 50 requests per second and increases with 50 requests per second
each 600 seconds, up to 300 requests per second. For the second test, the HPA is
added to the cluster and linearly increasing workload is again applied to the SaaS
application. The workload now rises up to 600 requests per second instead of up to
300. 110% of the  CPU request is selected as the point of scaling for the HPA. A second node
is made available for the HPA to schedule a new replica of the SaaS application on.