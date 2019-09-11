The SaaS application and the low priority application are co-located on a node.
The scaling point for the SaaS application HPA is again set to 110%.
A node is made available for the HPA to schedule a replica of the SaaS application on. 
The application is subjected to a linearly increasing workload starting 
at 50 requests per second and increasing with 50 requests
per second each 600 seconds, up to 600 requests per second.