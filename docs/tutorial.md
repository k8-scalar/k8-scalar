# Tutorial
For this K8-Scalar 101, we will go over the steps to stress testing a container-orchestrated database cluster using k8-scalar. Furthermore, additional details about infrastructure and operation are appended. 
  
## (1) Setup a Kubernetes cluster, Helm and install the Heapster monitoring service

The setup of a Kubernetes cluster depends on the underlying platform. The [Infrastructure](./tutorial.md#ii-infrastructure) section provides some information to setup a distributed cluster. 
This tutorial explains how to install [MiniKube](https://kubernetes.io/docs/tasks/tools/install-minikube/). Minikube allows to setup a Kubernetes cluster with one worker node on your local machine.

[Helm](https://helm.sh/) is utilised to deploy the distributed system of the experiment. Helm is a package manager for Kubernetes charts. These charts are packages of pre-configured Kubernetes resources. For this system, we provide three charts. A shared _monitoring-core_ is used across several experiments. This core contains _Heapster_, _Grafana_ and _InfluxDb_. The second chart provides a database cluster and the third the ARBA system with an experiment controller included.


### Prerequisites

 **Disclaimer**
 
This local minikube-based setup is not suitable for running scientific experiments. If you want accurate results for the example experiment on a single machine, your could try a minikube VM of 16 virtual CPU cores and 32 GB of virtual memory. But we have never tested this. Moreover Minikube only supports kubernetes clusters with one worker node (i.e. the minikube VM). it is better to run the different components of the K8-Scalar architecture on different VMs as illustrated in Section 3 of the related paper. See the __Infrastructure__ section at the end of this file for some advice on how to control the placement of Pods across VMs.  

**System requirements**
  * Your local machine should support VT-x virtualization
  * To run a minikube cluster, a VM with 1 *virtual* CPU core and 2GB *virtual* memory is sufficient but the cassandra instances will not fit.
  * One local VM with minimally 2 virtual CPU cores and 4GB virtual memory must be able to run on your machine in order to run 1 Cassandra instance. A VM with 4 virtual CPU cores and 8GB virtual memory is required to run the entire tutorial with 2 Cassandra instances.
 

**Install git:** 
  * MacOS: https://git-scm.com/download/mac
  * Linux Debian: sudo apt-get install git
  * Linux CentOS: sudo yum install git
  * Windows: https://git-scm.com/download/win. GitBash is by default also installed. Open a GitBash session and keep it open during the rest of the experiment
 
**Clone the K8-scalar GitHub repository:** 
  
```
git clone https://github.com/k8-scalar/k8-scalar/ && export k8_scalar_dir=`pwd`/k8-scalar
```

The `${k8_scalar_dir}` environment variable refers thus to the local directory on your system in which the k8-scalar GitHub project has been cloned. 

**Setup other environment variables:**

The tutorial provides a number of bash scripts to demonstrate the usage of K8-Scalar, These scripts contain environment variables which should be self-declarative.

  * `${MyRepository}` = the name of the Docker repository of the customized experiment-controller image (based on Scalar). Create in 
                        advance an account for the ${MyRepository} repository at https://hub.docker.com/. In the context of this tutorial, 
                        all experiments from the paper are stored in the t138 repository at docker hub.
  * `${my_experiment}` = the name of the directory under `${k8_scalar_dir}` where code and data of your current experiment is stored
  * `${my_username}`= your username that also corresponds with the name of your relative homedir


### For Mac OS:
Install [Homebrew](https://brew.sh/) if not yet installed:
```bash
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```


install kubectl, minikube and helm client
```bash
# Install VirtualBox
brew cask install virtualbox
# Install kubectl
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.25.0/bin/darwin/amd64/kubectl && chmod +x ./kubectl && sudo mv ./kubectl /usr/local/bin/kubectl
# Install MiniKube
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v1.25.0/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
# Start MiniKube
minikube start --no-vtx-check --driver=virtualbox --cpus 4 --memory 8192

```
Wait until the minikube worker node is ready. You can check the readiness of the worker node by running the following command:

```
$ kubectl get nodes
NAME       STATUS    ROLES     AGE       VERSION
minikube   Ready     <none>    21m       v1.9.0
```

Install Helm
```bash
# Install Helm client:
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```
### For Linux:

Install [VirtualBox](https://www.virtualbox.org/wiki/Linux_Downloads)

install kubectl, minikube and helm client
```bash
# Install kubectl:
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.25.0/bin/linux/amd64/kubectl && chmod +x ./kubectl && sudo mv ./kubectl /usr/local/bin/kubectl
#Install MiniKube
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v1.25.0/minikube-linux-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
# Start MiniKube with enough resources
minikube start --no-vtx-check --driver=virtualbox --cpus 4 --memory 8192
```

If you get an authorization error when running `kubectl get nodes`:
```
$ kubectl.exe get nodes
error: You must be logged in to the server (Unauthorized)
```
then you have to switch to the minikube kubectl context

```
$ kubectl config use-context minikube
Switched to context "minikube".

$ kubectl get nodes
NAME       STATUS    ROLES     AGE       VERSION
minikube   Ready     <none>    21m       v1.23.0
```

Install Helm
```bash
# Install Helm client
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

### For Windows:

Install [VirtualBox](https://www.virtualbox.org/wiki/Downloads). And reboot 

Open the GitBash desktop application. Make sure you run it as an **Administrator**. You find this option in the menu that appears when clicking on the right mouse button.

install kubectl, minikube and helm client
```bash
# Install kubectl
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.25.0/bin/windows/amd64/kubectl.exe && export PATH=`pwd`:$PATH

# Install minikube
curl -LO https://storage.googleapis.com/minikube/releases/v1.25.0/minikube-windows-amd64.exe && mv minikube-windows-amd64.exe minikube.exe && export PATH=`pwd`:$PATH
minikube start --no-vtx-check --driver=virtualbox --cpus 4 --memory 8192
```
Install Helm
```bash
# Install Helm client
curl -LO https://get.helm.sh/helm-v3.3.4-windows-amd64.zip && unzip helm-v3.3.4-windows-amd64.zip && export PATH=`pwd`/windows-amd64/:$PATH
```
## Deploy Heapster monitoring service
Now with Kubernetes and Helm installed, you should be able to install services on Kubernetes using Helm.

Let us add the monitoring capabilities of Heapstwe to the cluster using Helm. We install the _monitoring-core_ chart by the following command. This chart includes instantiated templates for the following Kubernetes services: Heapster, Grafana and the InfluxDb. 
First, check however, if there is still an old clusterrole defined named system:heapster. If it does, please remove it:
```
role=`kubectl get clusterrole | grep heapster | head -n1 | awk '{print $1;}'`
kubectl delete clusterrole $role
```
Then install the monitoring-core chart:

```
helm install ${k8_scalar_dir}/operations/monitoring-core --generate-name --namespace=kube-system
```

To check if all services are running execute the following command to see if all Pods of these services are running
```
$ kubectl get pods --namespace=kube-system
NAME                                    READY     STATUS    RESTARTS   AGE
heapster-76647b5d6c-ln7lp               1/1       Running   0          6m
kube-addon-manager-minikube             1/1       Running   0          17m
core-dns-54cccfbdf8-wstwt               3/3       Running   0          17m
monitoring-grafana-8fcc5f8d6-x49wv      1/1       Running   0          6m
monitoring-influxdb-7bf9b74f99-kpvr8    1/1       Running   0          6m
storage-provisioner                     1/1       Running   0          17m
...
```



## (2) Setup a Database Cluster
This _cassandra-cluster_ chart uses a modified image which resolves a missing dependency in one of Google Cassandra's image. Of course, this chart can be replaced with a different database technology. Do mind that Scalar will have to be modified for the experiment with implementations of desired workload generators for the Cassandra database. The next step will provide more information about this modification.
``` 
helm install ${k8_scalar_dir}/operations/cassandra-cluster --generate-name
```

## (3) Determine and implement desired workload type for the deployed database in Scalar
## For the course capita-selecta Distributed Systems you can skip this step!
This step requires some custom development for different database technologies. Extend Scalar with custom _users_ for your database which can read, write or perform more complex operations. For more information how to implement this, we refer to the [Cassandra User classes](../development/scalar/src/be/kuleuven/distrinet/scalar/users) and the [Cassandra Request classes](../development/scalar/src/be/kuleuven/distrinet/scalar/requests). These classes use the Datastax driver for Cassandra. When using the datastax driver, [the rules linked here](https://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra) must be adhered to!

Afterwards we want to build the application and copy the resulting jar.
If docker is not installed in your environment, you can try the docker version in minikube. Here, first login into the minikube VM by executing the `minikube ssh` command. However you will need to run the mvn command inside a docker container thaty is based on Java and has a volume attached to a dir on the minikube VM. 


```
# clone the k8-scalar project in a normal linux distribution with Docker installed 
git clone https://github.com/k8-scalar/k8-scalar/ && export k8_scalar_dir=`pwd`/k8-scalar

# Extend User with operations for your database in the directory below
cd ${k8_scalar_dir}/development/scalar/src/be/kuleuven/distrinet/scalar/users
vim ${myDatabase}User.java # Cfr CassandraWriteUser.java

# Building the project....
mvn package
#copy the Jar file in the lib directory of the [example-experiment](../example-experiment):
cp ${k8_scalar_dir}/development/scalar/target/scalar-1.0.0.jar ${k8_scalar_dir}/development/example-experiment/lib/scalar-1.0.0.jar

#Then, build a new image for the experiment-controller using the Dockerfile in the [example-experiment](../example-experiment).
docker build -t ${myRepository}/experiment-controller ${k8_scalar_dir}/development/example-experiment/
overwrite in the following command MyRepository_DOCKERHUB_PASSWRD with your secret password: 
# docker login -u ${MyRepository} -p MyRepository_DOCKERHUB_PASSWRD  
docker push ${MyRepository}/experiment-controller

```
Scalar is a fully distributed, extensible load testing tool with numerous features. Have a look at the [Scalar documentation](./scalar) for more information.

## (4) Deploying experiment-controller
Some experiment-controllers may need access to the API server. In this tutorial, we will give the experiment-controller cluster admin privileges so it can create, read, update and delete any information about any deployments in any namespace of the K8s cluster. This is of course very insecure and disallowed for clusters in production environments. Thus, only allowed for benchmarking purposes in closed lab scenarios.

## Creating secrets to enable access to Kubernetes API for experiment-controller pod 
When th erperiment-controller interact directly with the Kubernetes cluster, it uses _kubectl_ tool, as a normal user.

Note the following instructions work for minikube. Instructions for other kubernetes vendors are not exactly the same. We try to differentiate the common parts for all vendors and kubernetes-specific parts 

### Copying the kube config file and the secret

The next snippet creates the required keys for a cluster for any vendor. First, prepare a directory that contains all the required files. 


```bash
mkdir ${k8_scalar_dir}/operations/secrets
cd ${k8_scalar_dir}/operations/secrets
cp ~/.kube/config .
```
**Additional instructions for Minikube**

First the following keys need to be copied as well

```bash
cp ~/.minikube/ca.crt .
cp ~/.minikube/profiles/minikube/client.crt .
cp ~/.minikube/profiles/minikube/client.key .
```
Secondly, change all absolute paths in the  `config` file to the location at which these secrets are mounted by the `experiment-controller` and `arba` Helm charts, i.e. `/root/.kube`. The directories `minikube` and `minikube/profiles/minikube`  of the local machine must be changed to `/root/.kube`. You can either do it manually or modify and execute one of the following two sed scripts:

*Windows*

Replace your username stored in `$my_username` with `/root/.kube/` in file ./config. Unfortunately in windows this has to be done manually, e.g. if `$my_user_name` equals `eddy`: 
```
sed -i 's/C:\\Users\\eddy\\.minikube\\profiles\\minikube\\/\/root\/.kube\//g' ./config
sed -i 's/C:\\Users\\eddy\\.minikube\\/\/root\/.kube\//g' ./config
```

*Linux/MacOS*

```
sed -i "s/Users\/${my_username}\/.minikube\/profiles\/minikube\//root\/.kube\//g" ./config
sed -i "s/Users\/${my_username}\/.minikube\//root\/.kube\//g" ./config
```

### Creating the secret

Finally, the following command will create the secret. You will have to create the same secret in two different namespaces.  Do note that the keys required depend on the platform that you have your cluster deployed on.

```
#The secret for the experiment-controller that runs in default namespace
kubectl create secret generic kubeconfig --from-file . 
```

## Deploying the experiment-controller for the Cassandra workload

We deploy the experiment controller also a statefulset that can be scaled to multiple instances. To install the stateful set with one instance, execute the following command 

```
helm install ${k8_scalar_dir}/operations/experiment-controller --generate-name
```

## (5) Perform experiment for determining the mapping between SLA violations and resource usage metrics  
Before starting the experiment, we recommend using the `kubectl get pods --all-namespaces` command to validate that no error occured during the deployment. 

To determine the mapping we configure the experiment-controller pod to exercise a linearly increasing workload profile. The [stress.sh script](../development/example-experiment/bin/stress.sh] offers a user-friendly tool for configuring such workload profile. For example, the following command will tell Scalar to gradually increase the workload on the database cluster during 800 seconds:

```
kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 400 500:1500:1000
```
In this particular command, the workload is executed as a series of 2 Scalar runs. The duration of a single run is set at 400 seconds. The workload starts at a run of 500 requests per second and increases up to 1500 with an increment of 1000 requests per second. 
In order to find the specific workload volume, when the number of SLO violations start the increase, a more fine-grained workload with more runs is needed. For example, the following command tells Scalar to increase the worload with a delta throughput of 50 additional requests per second, and thus lasts 8400 seconds with 21 runs. 

```
kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 400 500:1500:50
```

The user guide of `stress.sh` is returned when running stress.sh without any option:

```
kubectl exec experiment-controller-0 -- bash bin/stress.sh
bin/stress.sh [OPTIONS] [ARGS]

 Performs an experiment to determine the threshhold to start scaling databases.

 Parameters:
   userload                             Specify user load or, the start user load, end user load and incrementing interval. (Format:  NN or NN:NN:NN)

 Options:
   -d | --duration              Specify the duration in seconds for each run. (Default: 60)
   -p | --pod                   The initial pod (Default: cassandra-0)
   -h | --help                  Display this message

 Note:
   This script MUST be executed from within the experiment directory.

 Examples:
   # Stress Cassandra with an user load of 125 requests per seconds for 200 seconds
   bin/stress.sh --duration 200 125

   # Executes the experiment: 10 users for first run, 20 users for second run, .., 100 users for last run.
   bin/stress.sh 10:100:10
```


### Inspect the experiment's results
Afterwards, experiment results include Scalar statistics and Grafana graphs. The Scalar results are found in the experiment-controller pod in the `/exp/var` directory as well as in the `/data/results` directory of the VM on which the experiment-controller pod runs. This snipper provides an easy way to copy the results to the local developer machine. 

```
# Open a shell to experiment-controller pod's Scalar results
$ kubectl exec -it experiment-controller-0 -- bash
root@experiment-controller:/exp/var# cd results/
root@experiment-controller:/exp/var/results# ls
run-1500.dat  run-500.dat

#OR: Open a shell to the minikube VM. 
#For Windows Users, execute the following commands in a regular Command Prompt App and not the Git Bash. 
#You need to set the minikube binary in the Windows Path environment variable via the Control Panel. 
#The Escape character in Vim is Control-C. 
$ minikube ssh
$ cd /data/results/results
$ ls
run-1500.dat  run-500.dat

#OR: SCP the /data/results directory from minikube to a local directory of your machine:
$ scp -r -i $(minikube ssh-key) docker@$(minikube ip):/data/results .
```
```
$vi run-500.dat
Experiment consisting of 1 runs and 1 request types:
        CassandraWriteRequest


Load,          Duration,      Troughput,     Capacity,      Slow requests, Timing accuracy
------------------------------------------------------------------------------------------
500,           60s,           21466,         357.767,       1.141%,        21.944ms


Breakdown of residence times for CassandraWriteRequest requests:

Load,          Min,           Max,           Mean,          Std. dev.,     Shape,         Scale          (successful)   (failed)       (error)        (conn_problem) (timed_out)    (redirected)   (no_result)
500            2.166          7756.775       309.948        695.622        0.199          1561.196       21507          0              0              0              0              0              0


Percentiles of sampled residence times (only an approximation if residence_times_sample_fraction is set):

CassandraWriteRequest (run 1, 500 users):
        50.0%   of requests handled in 142.039ms.
        90.0%   of requests handled in 613.618ms.
        95.0%   of requests handled in 958.478ms.
        99.0%   of requests handled in 5273.487ms.
        99.9%   of requests handled in 6851.968ms.
        99.99%  of requests handled in 7756.198ms.
```

In order to see all runs in sequential order you can execute a for loop as follows:

```bash
for i in `seq 1000 500 1500`; do cat run-$i.dat; done | more
```

In order to see the time when a run finished, execute a simple `ls`:

```bash
ls -l run-*.dat
```
In order to map a specific run to the resource usage graphs of Grafana, you need to log the time when the previous run has ended and the run of interested ended. Then you'll need to map these two timestamps to the grafana charts for the cassandra pod. 

You can open the grafana dashboard for the visualisation of resource usage graphs as follows (**Use HTTP and not HTTPS** for accesssing the grafana dashboard):

```
# Open the Grafana dashboard in your default browser and take relevant screenshots
$ minikube ip
192.168.99.100
#open the grafana dashboard in your favorite browser
open http://192.168.99.100:30345/
```
The Kubernetes cluster exposes thus the Grafana dashboard at node port 30345. Of course, the above script to open grafana is only valid when trying out the flow on MiniKube. For realistic clusters, you can access the grafana service at port 30345 on any Kubernetes node.

To see the resource usage graphs of the cassandra pod, Open the Pods dashboard page, and change the Namespace from `kube-system` into `default`. Note that although a default graph for Cassandra is provided for each resource type, you can also write your own detailed graph views as SQL like queries. To write such queries,  left-click on the area of the graph of interest; then click _Edit_ in the appeared pop-up menu. Grafana documentation can be found [here](http://docs.grafana.org/v4.1/guides/basic_concepts/).


### Test another workload profile
Underlying the experiment controller uses the scalar Java library. If you want to simulate other workloads than a linearly increasing stress workload profile, for example an oscillating workload profile, you have to run the scalar library directly in the experiment controller pod.

First, you'll need to define this workload profile in the file [experiment.properties](../development/scalar/conf/experiment.properties). An explanation overview of all the configuration options for defining a Scalar experiment is explained [here](./scalar/features.md).

The easiest way to change the experiment.properties file is to start a bash session inside the experiment-controller-0 Pod and edit the `etc/experiment.properties` file

```
#open bash session
kubectl exec -it experiment-controller-0 -- bash

#edit experiment.properties file
vi etc/experiment.properties
```

Before running the experiment, it is necessary to check if the experiment-controller and Cassandra pods are appropriately setup. The /bin/stress.sh file automatically executes the appropriate setup instructions. So the most easy way to appropriate setup the Pods is to run a dummy version of stress.sh. For example, `kubectl exec -it experiment-controller-0 bash bin/stress.sh --duration 1 1:2:1` does the trick.

To run the actual experiment, you need to start Scalar using `java`:
```
#run the experiment
java -jar lib/scalar-1.0.0.jar etc/platform.properties etc/experiment.properties > var/console_output.log e> var/error_output.log &
```
After the experiment is finished, you can inspect run-data as explained in Step 5. The only difference is that all run-data is now stored in one file. 


 To exec into the experiment controller pod run the following command:

```
kubectl exec -it experiment-controller-0 -- bash
```
Scalar is a fully distributed, extensible load testing tool with numerous features. Have a look at the [Scalar documentation](./scalar) for more information about how to configure and run it as a Java program directly.

# II. Infrastructure
You need to have a Kubernetes cluster, and the kubectl command-line tool must be configured to communicate with your cluster. For example, create a Kubernetes cluster on Amazon Web Services ([tutorial](https://kubernetes.io/docs/getting-started-guides/aws/)) or quickly bootstrap a best-practice cluster using the [kubeadm](https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/) toolkit. K8-scalar has been tested on kubernetes v1.9.x and v1.14.x. 

To install the monitoring system one a specific system you need to install the [monitoring-core-distributed chart](../operations/monitoring-core-distributed) instead of the monitoring-core chart. The K8s configuration expects that a node is explicitly labeled as a `monitoringNode` do deploy the heapster service, which is the central core of the monitoring system. So in order to ensure that the monitoring system will  deploy, execute the following command before or right after deploying the helm chart:


```
kubectl label node <node name> monitoringNode="yes"
```

```
helm install ${k8_scalar_dir}/operations/monitoring-core-distributed
```

In this tutorial, however, we will use a MiniKube deployment on our local device.
This is just for demonstrating purposes as the resources provided by a single laptop are unsufficient for valid experiment results.
You can, however, follow the same exact steps on a multi-node cluster.
For a more accurate reproduction scenario, we suggest adding more labels to each node and add them as constraints to the YAML files of the relevant Kubernetes objects via a [nodeSelector](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector). As such different Kubernetes objects such as experiment-controller and the cassandra instance will not be created on the same node, as presented in the related paper.

# III. Operations
## Creating secrets to enable access to Kubernetes API for experiment-controller pod 
The autoscaler and erperiment-controller interact directly with the Kubernetes cluster. The _kubectl_ tool, which is used for this interaction, requires configuration. Secrets are used to pass this sensitive information to the required pods. 
Note the following instructions work for minikube. Instructions for other kubernetes vendors are not exactly the same. We try to differentiate the common parts for all vendors and kubernetes-specific parts 

### Copying the kube config file and the secret

The next snippet creates the required keys for a cluster for any vendor. First, prepare a directory that contains all the required files. 


```bash
mkdir ${k8_scalar_dir}/operations/secrets
cd ${k8_scalar_dir}/operations/secrets
cp ~/.kube/config .
```
**Additional instructions for Minikube**

First the following keys need to be copied as well

```bash
cp ~/.minikube/ca.crt .
cp ~/.minikube/profiles/minikube/client.crt .
cp ~/.minikube/profiles/minikube/client.key .
```
Secondly, change all absolute paths in the  `config` file to the location at which these secrets are mounted by the `experiment-controller` and `arba` Helm charts, i.e. `/root/.kube`. The directories `minikube` and `minikube/profiles/minikube`  of the local machine must be changed to `/root/.kube`. You can either do it manually or modify and execute one of the following two sed scripts:

*Windows*

Replace your username stored in `$my_username` with `/root/.kube/` in file ./config. Unfortunately in windows this has to be done manually, e.g. if `$my_user_name` equals `eddy`: 
```
sed -i 's/C:\\Users\\eddy\\.minikube\\profiles\\minikube\\/\/root\/.kube\//g' ./config
sed -i 's/C:\\Users\\eddy\\.minikube\\/\/root\/.kube\//g' ./config
```

*Linux/MacOS*

```
sed -i "s/Users\/${my_username}\/.minikube\/profiles\/minikube\//root\/.kube\//g" ./config
sed -i "s/Users\/${my_username}\/.minikube\//root\/.kube\//g" ./config
```

### Creating the secret

Finally, the following command will create the secret. You will have to create the same secret in two different namespaces.  Do note that the keys required depend on the platform that you have your cluster deployed on.

```
#Secret for ARBA that runs in the kube-system namespace
kubectl create secret generic kubeconfig --from-file . --namespace=kube-system

#The same secret for the experiment-controller that runs in default namespace
kubectl create secret generic kubeconfig --from-file . 
```

## Configuration of other Kubernetes objects 
Several Kubernetes resources can optionally be fine-tuned. Application configuration is done by setting environment variables. For example, the Riemann component can have a strategy configured or the Cassandra cpu threshold at which it should scale. 

Finally, the resource requests and limits of the Cassandra pod can also be adjusted. These files can be found in the `operations` subdirectory, e.g. the Cassandra YAML file can be found in [operations/cassandra-cluster/templates](../operations/cassandra-cluster/templates/cassandra-statefulset.yaml). For this MiniKube tutorial we have set for resource Requests lower than the resource limits in comparison to the configuration of Cassandra instances in the [scientifically evaluated experiments of the associated paper](../experiments/LMaaS)



# IV. Development
The goal of this section is to explain how to modify the K8-Scalar examplar to experiment with other types of autoscalers and other types of services.

In order to replace the default ARBA autoscaler with another autoscaler,
it is important to understand that the stable parts of the
current implementation are Kubernetes and Heapster. Heapster
can store the collected metrics into different backends, which are
referred to as sinks. Heapster currently supports [16 different sink
types](https://github.com/kubernetes/heapster/blob/master/docs/sink-configuration.md), including the Riemann sink. To plug-in another autoscaler,
the autoscaler should be compatible with one of these sink
types.

The REST-based interface of the Scaler service of ARBA also
aims to offer a portable specification of scaling actions for any type
of application and any type of container orchestration framework.

Finally, Scalar’s implementation can be easily extended with
additional functionality.  It is of course also possible to use another
benchmarking tool as Experiment-Controller if that tool is more appropriate
for a particular experiment. The ARBA-with-experiment-
Controller must then be changed with a Docker image for that
tool.
