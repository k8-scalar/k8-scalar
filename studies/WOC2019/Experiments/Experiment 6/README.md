The SaaS application is deployed in isolation and configured so that it processes the
requests in a CPU intensive manner. Its CPU request is set to 1.5 CPU.

Two separate tests are run.
First, a bursty workload is applied to the SaaS application consisting of five minutes of 60 requests per second 
followed by a one minute peak of 250 requests per second. This pattern is repeated 20 times.
In the second test, the the duration of the low workload inbetween the bursts is increased to seven minutes.