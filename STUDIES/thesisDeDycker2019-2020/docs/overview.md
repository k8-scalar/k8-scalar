# K8-Scalar: a workbench to compare autoscalers of container-orchestrated services

## Scope

The K8-Scalar artifact is an easy-to-use and extensible workbench
exemplar for implementing and evaluating different self-adaptive
approaches to autoscaling container-orchestrated services. Container
technology such as Docker  and container orchestration frameworks such
as Kubernetes  have been pushed by the Linux Foundation as the
contemporary set of platforms and tools for deploying and managing
cloud-native applications . Docker is a popular tool for deploying
software using fast and light-weight Linux containers . Moreover,
Docker’s approach to image distribution, which enables developers to
store a specific configuration of a software component as a portable and
light-weight Docker image that can be downloaded from a local or central
Docker registry. The latter advantage also helps to address the above
mentioned necessity for repeating experiments in identically configured
testing environments to improve reproducible research . Container
technology also has the potential to improve consistency of resource
allocation parameters across different computers with the same CPU clock
frequency.

The K8-Scalar artifact has been used and validated in the context of
autoscalers for database clusters. Although autoscalers for database
clusters  or multi-tier applications  have been researched, developing
an effective autoscaler for databases is still an art, rather than a
science. First, there is no one-size-fits-all solution as autoscalers
must be specifically customized for specific databases. For example,
auto-scaling makes only sense when a database cluster is load balanced
such that adding a new database instance will reduce the load of an
existing instance. The load balancing algorithms that are specifically
designed for that purpose are database-specific, however, and therefore
the autoscaler’s design must take into account how fast load balancing
algorithm adapt to requested cluster reconfigurations. Secondly, adding
or removing an instance should be cost-efficient in the sense that it
should not require extensive (re)shuffling of data across existing and
new database instances. Thirdly, detecting imminent SLA violations
accurately requires multiple type of metrics to be monitored and
analyzed simultaneously so that no wrong scaling decisions are triggered
by temporary spikes in resource usage or performance metrics. Such wrong
scaling decisions can be very costly an actually hurt elasticity instead
of improving it.

## Building blocks

 K8-Scalar integrates and customizes Scalar , a generic platform for
    evaluating the scalability of large-scale systems, with support for
    **evaluating autoscalers for container-orchestrated database
    clusters**.

K8-Scalar extends Kubernetes with an advanced autoscaler for
    database clusters, based on the Riemann event processor  that allows
    for simultaneous analysis of multiple metrics and composition of
    multiple event-based conditions. This Advanced Riemann-Based
    Autoscaler (ARBA) comes with a set of elastic scaling policies,
    which are based on the Universal Law of Scalability  and the Law of
    Little , and that have been implemented and evaluated in the context
    of a case study on using Cassandra in a Log Management-as-a-Service
    platform .

## Content

The artifact is stored and publicly available on GitHub at the following
URL: <https://github.com/k8-scalar/k8-scalar>. The current release
includes:

1.  a detailed, step-by-step hands-on tutorial that relies on Helm, a
    command-line interface and run-time configuration management server
    for creating and managing the *Helm charts* . A Helm chart is a
    highly-configurable deployment package that encapsulates
    inter-dependent Kubernetes objects such as services, configuration
    setting or authentication credentials. The tutorial presents
    extensive and easy-to-following instructions for the following
    steps:
    
      - Setting up Kubernetes in a development environment using
        Minikube  or across a cluster of machines using the universal
        kubeadm deployment tool .
    
      - Deploying the Kubernetes’s monitoring service Heapster  with a
        Grafana visual monitoring dashboard.
    
      - Deploying the Cassandra database as a Kubernetes StatefulSet
        object .
    
      - Developing and configuring a specific Scalar experiment for the
        Cassandra database and a specific type of workload (e.g 90%
        reads and 10% writes).
    
      - Building a Docker image of the Scalar experiment.
    
      - Deploying the Scalar experiment as a StatefulSet.
    
      - Running the Scalar experiment with a different linearly
        increasing workload profiles for determining the appropriate
        resource thresholds where elastic scaling actions should be
        triggered in order to avoid imminent SLA violations.
    
      - Configuring a concrete autoscaling strategy of the included
        Advanced Rieman-Based Autoscaler (ARBA) and deploying it.
    
      - Running a Scalar experiment to evaluate the behavior of a
        particular autoscaling strategy. The Grafana visual monitoring
        dashboard allows to monitor resource usage and scaling actions
        at run-time. Scalar’s statistical results can be further
        analyzed off-line. Scalar also monitors the performance of the
        experiment itself in order to detect any undesired scalability
        or performance bottlenecks in the Scalar code itself.

2.  A library with development and operational code artifacts of
    K8-Scalar. This archive thus consists of two logical parts:
    
      - A `development` directory containing the following
        subdirectories:
        
        1.  The `cassandra` sub-directory that contains a Dockerfile for
            building the Cassandra image that we have used in our
            experiments.
        
        2.  A set of sub-directories that encapsulate development code
            for some of the MAPE-K components of the ARBA autoscaler:
            
              - The `riemann` sub-directory that contains the code of
                ARBA’s Analyzer component (which is dependent on
                Kubernetes’ monitoring service *Heapster* and is written
                on top of the *Riemann event processor* .
            
              - The `scaler` sub-directory that contains the code of the
                Executor component for performing scaling actions of the
                Cassandra database.
        
        3.  The set of sub-directories with development code for
            K8-Scalar experiments:
            
              - The `scalar` sub-directory that contains all the code
                for extending Scalar with support for (i) evaluating
                autoscalers for the Cassandra databases and (ii) mapping
                SLAs to resource thresholds.
            
              - The `example-experiment` sub-directory which offers a
                template for configuring a specific K8-Scalar experiment
                (e.g. with a specific particular workload profile).
        
        4.  A set of sub-directories with development code for
            configuring a number of Kubernetes tools on which K8-Scalar
            relies:
            
              - The `grafana` sub-directory that contains a slightly
                customized Dockerfile for building the monitoring
                dashboard image that visualizes monitoring data from
                Heapster.
            
              - The `helm` directory that contains configurations for a
                running Kubernetes cluster such that the Helm software
                works properly on top of that Kubernetes cluster.
        
        The above sub-directories, except the `helm` directory, also
        contain a Dockerfile for building a Docker image of the
        respective software component in these sub-directories.
    
      - An `operations` directory containing the following
        sub-directories with Helm packages for deploying the following
        Kubernetes resources:
        
        1.  The Cassandra-cluster in the `cassandra-cluster` directory.
        
        2.  The Heapster monitoring service in the `monitoring-core`
            directory.
        
        3.  The ARBA autoscaler in the `arba` subdirectory which deploys
            the riemann-based Analyzer component with a particular
            strategy configured and the Scaler component.
        
        4.  The Scalar experiment-controller in the
            `experiment-controller` directory.

3.  The development and operations artifacts for all experiments
    presented in the scholarly paper in the `experiments/LMaaS`
    sub-directory.

4.  This `docs` directory containing the documentation of K8-Scalar. See [index.md](./index.md) for the table of contents

## Tested platforms

K8-Scalar runs on multiple platforms: Linux VMs, Linux bare metal, OS X,
and Windows. It has been tested and used extensively on Linux Ubuntu
Xenial VMs and with the *kubeadm* universal Kubernetes deployment tool .
The detailed hands-on experience for running K8-Scalar on a development
desktop computer using the *minikube* deployment tool  runs on Linux
bare metal, Windows 7, Windows 10, and Mac OS . It has been tested on
Mac OS and Windows 10. System requirements for running the hands-on
tutorial:

  - Your local machine should support VT-x virtualization

  - To run the Kubernetes cluster on your local machine, a VM with 1 CPU
    core and 2GB is memory is sufficient but the cassandra instances
    will not fit.

  - To run 1 cassandra instance, a VM with minimally 2 virtual CPU cores
    and 4GB **virtual** memory must be able to run on your machine. In
    order to run 2 Cassandra instances, 4 virtual CPU cores and 8GB of
    virtual memory is needed.

**Disclaimer.** The minikube-based setup of the tutorial is not suitable
for running scientific experiments. Minikube only supports kubernetes
clusters with one worker node (i.e. the minikube VM). It is better to
run the different components of the K8-Scalar architecture on different
VMs as illustrated in Section 3 of the related scholarly paper. Kubeadm
is needed for setting up Kubernetes clusters on multiple VMs.

# License

The artifact is available under the Apache License version 2.0 ([
http://www.apache.org/licenses/LICENSE-2.0](
http://www.apache.org/licenses/LICENSE-2.0).

