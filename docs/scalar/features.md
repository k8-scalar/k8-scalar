# Features of scalar

## Development

Open the project in Eclipse. Click on project -> Configure -> Convert to Maven project. All dependencies are managed via Maven
To execute a dummy Scalar experiment on your local machine, you can start Scalar with the default `experiment.properties` and `platform.properties` 
configuration files. Start the Jave class Launcher.class in Eclipse with as command-line arguments `conf/platform.properties` and `conf/experiment.properties`.


You also compile the Scalar code at the command line with `mvn package`.  You execute the dummy Scalar experiment as 
```
java -jar target/scalar-1.0.0.jar conf/platform.properties conf/experiment.properties
```
To include Scalar in your own Maven project, you can use the `scalar-project-pom.xml` as a template 
The source code does also include unit tests that coverage most of Scalar's functionality 

To develop a particular workload type, you have to design different subclasses of the `be.kuleuven.distrinet.scalar.core.User` and `be.kuleuven.distrinet.scalar.core.Request classes. 
The [following paper](./heyman_preuveneers_joosen.pdf) illustrates in detail how to implement such subclasses.

## Operations

The following features are supported by scalar

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

In this file, th enormalized values of 0/90, 10/90, 60/90/ and 20/90 requests have a think time of respectively between 0 and 500ms, 
500 and 1000ms, 1000 and 1500ms.

  * UniformThinkTimeStrategy, generates think times according to a random uniform distribution of which the `mean` is configured in experiments.properties, 
    and which is uniformly distributed in the interval `[0, 2*mean[`.

* De SingleShotThinkTimeStrategy which lets a user do a single request and afterwards never again

To add a think time strategy, define a subclass of the `ThinkTimeStrategy` and  `ThinkTimeStrategyFactory` classes
of the package `be.kuleuven.distrinet.scalar.users.scheduling`.


 
### Sypport for testing mult-tenant applications
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
