## Features of scalar

### Configuration of workload profile
To configure a particular oscillating workload profile, Scalar supports the following configuration options:

```
The load profile varies between the min and max numbers of concurrent users. It
# starts low, then ramps up to a peak which is sustained for a specific time, and
# then gradually ramps down again to the minimum load. Both ramping up and down is
# done linearly. Therefore, the effective load, in function of time, looks something
# like this:
#                               ____
#                              /    \
#            ____             /      \
#           /    \           /        \
#          /      \         /          \
#     ____/        \_______/            \______ ...
#
# The number of concurrent users for peak load for the entire cluster. For each
# number in this comma separated list, a new run is started.
user_peak_load=10

# The fraction of concurrent users used for warm up (i.e., in the interval [0,1]).
# For a peak load of 100 and a warmup fraction of 0.1, 10 users would be started
# during warmup. 
user_warmup_fraction=1

# The initial period of minimum load, in secconds.
user_warmup_duration=0

# The duration of the ramping up period, in seconds.
user_ramp_up_duration=0

# The duration of the peak load, in seconds.
user_peak_duration=10

# The duration of the ramping down period, in seconds.
user_ramp_down_duration=0

# The final cool down period of minimum load, in seconds.
user_cooldown_duration=0

# How long to wait between multiple consecutive runs, in seconds.
user_wait_inbetween_runs=0
```
### ThinkTime Strategies

You can control the time between two requests from the same user as think time strategies. 
These strategies are determined at run-time. The following existing strategies have already been developed:

  * ConstantThinkTimeStrategy: a  constant think time for every user. The specific time can be configured in experiment.properties.
  * ExponentialThinkTimeStrategy, which generates think times according to an exponential distribution whare the average is configured in experiment.properties. 
  * HistogramThinkTimeStrategy, which generates think times in accordance with a histogram dat is specified in a separate CSV  file with the following format:
```
 # Bin start, bin end, count
 0; 500; 0
 500; 1000; 10
 1000; 1500; 60
 1500; 2000; 20
 ```

In this file, the enormalized values of 0/90, 10/90, 60/90/ and 20/90 requests have a think time of respectively between 0 and 500ms, 
500 and 1000ms, 1000 and 1500ms.

  * UniformThinkTimeStrategy, generates think times according to a random uniform distribution of which the `mean` is configured in experiments.properties, 
    and which is uniformly distributed in the interval `[0, 2*mean[`.

* De SingleShotThinkTimeStrategy which lets a user do a single request and afterwards never again

To add a think time strategy, define a subclass of the `ThinkTimeStrategy` and  `ThinkTimeStrategyFactory` classes
of the package `be.kuleuven.distrinet.scalar.users.scheduling`.

### Customizable via plugins
It is possible to extend Scalar with additional functionality by means of plugins.
The currently available plugins are listed in the [platform.properties file](../../development/scalar/conf/experiment.properties).


```
## PLUGIN CONFIGURATION
plugins=\
	be.kuleuven.distrinet.scalar.plugin.ExperimentalPropertiesLoader,\
	be.kuleuven.distrinet.scalar.plugin.ExperimentalResultsPublisher,\
  	be.kuleuven.distrinet.scalar.plugin.SummaryGenerator,\
  	be.kuleuven.distrinet.scalar.plugin.RequestReporter
#     be.kuleuven.distrinet.scalar.plugin.ClusterStarter
#	be.kuleuven.distrinet.scalar.plugin.GnuPlotGenerator,\
#	be.kuleuven.distrinet.scalar.plugin.HazelcastMonitor,\
#	be.kuleuven.distrinet.scalar.plugin.ClusterMonitor,\
#	be.kuleuven.distrinet.scalar.plugin.NodeMonitor, \
#   be.kuleuven.distrinet.scalar.plugin.StabilityMonitor

```


### Rich user behavior and interactions
It is possible to implement all kinds of interactions between Users via a DataProvider. Consider the example of a system administrator User of the SaaS application who approves a request of a customer User to create a new tenant. In a real life system. the approval action will generate an email with a URL to  verify the account creation. This URL is a piece of data that the customer User needs to perform the email verification.

The [following paper](./heyman_preuveneers_joosen.pdf) illustrates in detail how to configure and use a DataProvider for implementing such real-life User interactions.

### Changing the target host
To change the host on which the workload must be exercised, change the targeturl property  

### Custom experiment.properties
It is also possible to add custom properties to configure the experment.
Suppose you want to test the application of a particular customer, also referred to as tenant. 

You can then add for example the following two properties to `experiment.properties` :

```
use_tenant_id: true
tenant_id: 23246
```

and let a User class implementation retrieve the values of these two properties as follows:

```
_useTenantID = data().getAsBoolean("use_tenant_id");
if (_useTenantID) { 
  int tenantID = data().getAsInt("tenant_id");
}

```



### Configuring a distributed Scalar experiment with multiple Scalar nodes

 
To start up a Scalar experiment with multiple Scalar nodes, you need to specify the number of scalar nodes in [experiment.properties](../../development/scalar/conf/experiment.properties):

```
# Scalar cluster size. The master load generator will wait until this many nodes 
# (including the master) report ready.
scalar_minimal_cluster_size=3
```

Currently, this feature is not yet fully integrated with Kubernetes, so you cannot use the experiment-controller Helm chart for this. Instead you need to configure the `ClusterStarter` plugin that will create Scalar instances on different VMs. 
To use this plugin, you need to activate the ClusterStarter plugn in the [platform.properties file](../../development/scalar/conf/experiment.properties). Secondly, you need to configure the clusterstarter plugin in the same platform.properties file. See an extract of required configuration for this file below :

```
## PLUGIN CONFIGURATION
plugins=\
	be.kuleuven.distrinet.scalar.plugin.ExperimentalPropertiesLoader,\
	be.kuleuven.distrinet.scalar.plugin.ExperimentalResultsPublisher,\
  	be.kuleuven.distrinet.scalar.plugin.SummaryGenerator,\
  	be.kuleuven.distrinet.scalar.plugin.RequestReporter,\
   be.kuleuven.distrinet.scalar.plugin.ClusterStarter
#	be.kuleuven.distrinet.scalar.plugin.GnuPlotGenerator,\
#	be.kuleuven.distrinet.scalar.plugin.HazelcastMonitor,\
#	be.kuleuven.distrinet.scalar.plugin.ClusterMonitor,\
#	be.kuleuven.distrinet.scalar.plugin.NodeMonitor, \
#   be.kuleuven.distrinet.scalar.plugin.StabilityMonitor

# ClusterStarter config
########################
#
# The list of nodes that form the Scalar cluster. Can be just "localhost", or can be
# a comma separated list of hostnames. Leave empty for autodiscovery.
scalar_cluster_nodes=localhost,host2,host3

# The user account to use to access the other cluster nodes over ssh.
cluster_starter_username=ec2-user

# The private keyfile to use to access the other cluster nodes over ssh.
cluster_starter_key=~/.ssh/id_rsa

# Where scalar.jar can be found (or can be installed if not present) on the
# other cluster nodes. End with a path separator (i.e., / on unix).
cluster_starter_local_working_dir=/home/ec2-user/
cluster_starter_remote_working_dir=/home/ec2-user/
cluster_starter_scalar_jar=scalar.jar

# Whether to upload the jar first, before starting.
cluster_starter_upload_jar=false

# Connection timeout in seconds for starting other Scalar nodes.
cluster_starter_connect_timeout=10
```

In the future, we'll envision that the experiment-controller statefulset is automatically scaled to 3 instances and that the different Scalar containers auto-discover themselves. You can already manually try this out by setting the `replicas` field of [experiment-controller-statefulset.yaml file](../../operations/experiment-controller/templates/experiment-controller-statefulset.yaml) to 3. The cluster starter plugin then does not need to be activated.


### Support for testing multi-tenant applications
Suppose you have an application that is used by differnt customers and different customer prefer to use different combinations of features.
Such an application supports typically a configuration interface that allows customers to activate the desired feature. 
In order to test the scaleability of every combination of features, you can configure Scalar to automate this in the `featuremapping.conf` file. For each feature it can be specified how Scalar must activate or deactivate a feature

More specifically,. featuremapping.conf is a JSON array of objects as follows

```

{
  "feature": "Logging",
  "enable": {
    "helpers": {
      "be.preuveneers.mobicent.scalar.config.MobiCentFeatureConfig": "enableLogging"
    }
  },
  "disable": {
    "helpers": {
      "be.preuveneers.mobicent.scalar.config.MobiCentFeatureConfig": "disableLogging"
    }
  }
}

```
Suppose that je activate the Logging feature:
```feature_model_config=Logging```

Then, Scalar will start an Java object of the  `be.preuveneers.mobicent.scalar.config.MobiCentFeatureConfig` class and invoke the method 
`enableLogging()`. This helper class must inherit from aanmaken the  `FeatureHelper` subclass.

If Logging is not included in in the `feature_model_config` value as follows:
```
feature_model_config=SomeFeature,SomeOtherFeature
```
Then, Scalar will create during the start of the experiment also an object of the `be.preuveneers.mobicent.scalar.config.MobiCentFeatureConfig` class but invoke the method `disableLogging()`.

