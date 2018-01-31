# K8-Scalar
For this K8-Scalar 101, we will go over the steps to implement and evaluate elastic scaling policies in container-orchestrated database clusters using the Advanced Riemann-Based Autoscaler (ARBA). Furthermore, additional details about infrastructure and operation are appended. The goal is to enable modification of the K8-Scalar examplar to experiment with other types of autoscalers and other types of applications.

# Comparing autoscalars for container-orchestrated database clusters
This tutorial provides more practical know-how for the related paper. Eight steps allow us to effectively implement and evaluate elastic scaling strategies for specific workloads.

The setup of a Kubernetes cluster depends on the underlying platform. The _infrastructure_ section provides some references to get started. If you just want to try out the tutorial on your local machine, then you can run directly the bash scripts that are provided by this tutorial. This tutorial installs [MiniKube](https://kubernetes.io/docs/tasks/tools/install-minikube/). 


## Prerequisites


**System requirements**
  * Your local machine should support VT-x virtualization
  * One local VM with 4 virtual CPU cores and 8GB virtual memory must be able to run on your machine
  
**Install git:** 
  * MacOS: https://git-scm.com/download/mac
  * Linux Debian: sudo apt-get install git
  * Linux CentOS: sudo yum install git
  * Windows: https://git-scm.com/download/win. GitBash is by default also installed. Open a GitBash session and keep it open during the rest of the experiment
 
**Clone the K8-scalar GitHub repository and set the k8_scalar_dir variable:** 
  
```bash
git clone https://github.com/k8-scalar/k8-scalar/ && export k8_scalar_dir=`pwd`/k8-scalar
```


**Setup other environment variables**

```bash
# Note1: This tutorial contains many code snippets for MacOS, Linux or Windows. They are only tested on MacOS and Windows
# Note2: The snippets contain environment variables which should be self-declarative. Do not forget to specify them:
# - ${k8_scalar_dir} = the local directory on your system in which the k8-scalar GitHub project has been cloned
# - ${MyRepository} = the name of the Docker repository of the customized experiment-controller image (based on Scalar). Create in 
#                     advance an account for the ${MyRepository} repository at https://hub.docker.com/ 
# - ${my_experiment} = the name of the directory where code and data of your current experiment is stored
```

  
## (1) Setup a Kubernetes cluster, Helm and install the Heapster monitoring service__  

[Helm](https://github.com/kubernetes/helm) is utilised to deploy the distributed system of the experiment. Helm is a package manager for Kubernetes charts. These charts are packages of pre-configured Kubernetes resources. For this system, we provide three charts. A shared _monitoring-core_ is used across several experiments. This core contains _Heapster_, _Grafana_ and _InfluxDb_. The second chart provides a database cluster and the third the ARBA system with an experiment controller included.


### For Mac OS:
install kubectl, minikube and helm client
```bash
# Install VirtualBox
brew cask install virtualbox
# Install kubectl
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.9.0/bin/darwin/amd64/kubectl && chmod +x ./kubectl && sudo mv ./kubectl /usr/local/bin/kubectl
# Install MiniKube
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.24.1/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
# Start MiniKube
minikube start --cpus 4 --memory 8192

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
minikube   Ready     <none>    21m       v1.9.0
```

Install Helm
```
# Install Helm client:
curl -LO https://kubernetes-helm.storage.googleapis.com/helm-v2.8.0-darwin-amd64.tar.gz && tar xvzf helm-v2.8.0-darwin-amd64.tar.gz && chmod +x ./darwin-amd64/helm && sudo mv ./darwin-amd64/helm /usr/local/bin/helm
# Install Helm server on Kubernetes cluster
helm init
```
### For Linux OS on bare-metal:

Install [VirtualBox](https://www.virtualbox.org/wiki/Linux_Downloads)

install kubectl, minikube and helm client
```bash
# Install kubectl:
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.9.0/bin/linux/amd64/kubectl && chmod +x ./kubectl && sudo mv ./kubectl /usr/local/bin/kubectl
#Install MiniKube
curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
# Start MiniKube with enough resources
minikube start --cpus 4 --memory 8192
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
minikube   Ready     <none>    21m       v1.9.0
```

Install Helm
```
# Install Helm client
curl -LO https://kubernetes-helm.storage.googleapis.com/helm-v2.8.0-linux-amd64.tar.gz && tar xvzf helm-v2.8.0-linux-amd64.tar.gz && chmod +x ./linux-amd64/helm && sudo mv ./linux-amd64/helm /usr/local/bin/helm
# Install Helm server on Kubernetes cluster
helm init
```

## For Windows:

Install [VirtualBox](https://www.virtualbox.org/wiki/Downloads)

Open the GitBash desktop application

install kubectl, minikube and helm client
```bash
# Install kubectl
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.9.0/bin/windows/amd64/kubectl.exe && export PATH=$PATH:`pwd`

# Install minikube
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-windows-amd64.exe && mv minikube-windows-amd64.exe minikube.exe && export PATH=$PATH:`pwd`
minikube start --cpus 4 --memory 8192
```

If you get an authorization error when running `kubectl get nodes`:
```
$ kubectl.exe get nodes
error: You must be logged in to the server (Unauthorized)
```
You have to switch to the minikube kubectl context

```
$ kubectl config use-context minikube
Switched to context "minikube".

$ kubectl get nodes
NAME       STATUS    ROLES     AGE       VERSION
minikube   Ready     <none>    21m       v1.9.0
```

Install Helm
```
# Install Helm client
curl -LO https://kubernetes-helm.storage.googleapis.com/helm-v2.8.0-windows-amd64.tar.gz && tar xvzf helm-v2.8.0-windows-amd64.tar.gz && export PATH=$PATH:`pwd`/windows-amd64/
# Install Helm server on Kubernetes cluster
helm init
```

Afterwards, we want to add the monitoring capabilities to the cluster using Helm. We install the _monitoring-core_ chart by the following command. This chart includes instantiated templates for the following Kubernetes services: Heapster, Grafana and the InfluxDb. 
```bash
helm install ${k8_scalar_dir}/operations/monitoring-core
```

To check if all services are running execute the following command to see if all Pods of these services are running
```bash
$ kubectl get pods --namespace=kube-system
NAME                                    READY     STATUS    RESTARTS   AGE
heapster-76647b5d6c-ln7lp               1/1       Running   0          6m
kube-addon-manager-minikube             1/1       Running   0          17m
kube-dns-54cccfbdf8-wstwt               3/3       Running   0          17m
kubernetes-dashboard-77d8b98585-qqkmx   1/1       Running   0          17m
monitoring-grafana-8fcc5f8d6-x49wv      1/1       Running   0          6m
monitoring-influxdb-7bf9b74f99-kpvr8    1/1       Running   0          6m
storage-provisioner                     1/1       Running   0          17m
tiller-deploy-7594bf7b76-598xv          1/1       Running   0          7m
```



## (2) Setup a Database Cluster__  
This _cassandra-cluster_ chart uses a modified image which resolves a missing dependency in one of Google Cassandra's image. Of course, this chart can be replaced with a different database technology. Do mind that Scalar will have to be modified for the experiment with implementations of desired workload generators for the Cassandra database. The next step will provide more information about this modification.
```bash 
helm install ${k8_scalar_dir}/operations/cassandra-cluster
```

## (3) Determine and implement desired workload type for the deployed database in Scalar__  
This step requires some custom development for different database technologies. Extend Scalar with custom _users_ for your database which can read, write or perform more complex operations. For more information how to implement this, we refer to the [Cassandra User classes](development/scalar/src/be/kuleuven/distrinet/scalar/users) and the [Cassandra Request classes](development/scalar/src/be/kuleuven/distrinet/scalar/requests). Afterwards we want to build the application and copy the resulting jar:
```bash
# Extend User with operations for your database in the directory below
cd ${k8_scalar_dir}/development/scalar/src/be/kuleuven/distrinet/scalar/users
vim ${myDatabase}User.java # Cfr CassandraWriteUser.java

# After building the project, copy the resulting Jar file to the experiment-controller image
mv ${k8_scalar_dir}/development/scalar/target/scalar-1-0-0.jar ${k8_scalar_dir}/development/example-experiment/lib/scalar-1-0-0.jar

# Configure the experiment-controller's workload
cd ${k8_scalar_dir}/development/example-experiment/etc/
vim experiment-template.properties # Configure user_implementations, do not modify user_implementations_prestart
``` 

Finally, build a new image for the experiment-controller
```bash
docker build -t ${myRepository}/experiment-controller ${k8_scalar_dir}/development/example-experiment/
# overwrite in following command MyRepository_DOCKERHUB_PASSWRD with your secret password: 
# docker login -u ${myRepository} -p MyRepository_DOCKERHUB_PASSWRD  
docker push ${myRepository}/experiment-controller
```

Scalar is a fully distributed, extensible load testing tool with a numerous features. I recommend checking out https://distrinet.cs.kuleuven.be/software/scalar/ for more information.

## (4) Deploying ARBA__  
Before deploying, check out the [Operations section](README.md#operations) below in this document for performing the necessary Kubernetes secret management and resource configuration. The secret management is mandatory as ARBA requires this to communicate with the Master API of the Kubernetes cluster.
```bash
helm install ${k8_scalar_dir}/operations/arba-with-experiment-controller
```

## (5) Perform experiment for determining the mapping between SLA violations and resource usage metrics  
Before starting the experiment, we recommend using the `kubectl get pods --all-namespaces` command to validate that no error occured during the deployment. Finally, we can start the experiment by executing the following command:
```bash
kubectl exec experiment-controller -- bash bin/stress.sh --duration 400 500:1500:1000
```

This command will tell Scalar to gradually increase the workload on the database cluster. The workload is executed as a series of runs. The duration of a single run is set at 400 seconds. The workload starts at a run of 500 requests per second and increases up to 1500 with an increment of 1000 requests per second. For these arguments, the experiment will consist thus of 2 runs and last 800 seconds. Afterwards, experiment results include Scalar statistics and Grafana graphs. The Scalar results are found in the pod experiment-controller pod in the `/exp/var` directory. The Kubernetes cluster exposes a Grafana dashboard at port 30345. Some default graphs are provided, but you can also write your own queries. This snipper provides an easy way to copy the results to the local developer machine. Ofcourse, the second command is only valid when trying out the flow on MiniKube. For realistic clusters, you should determine the IP of any Kubernetes node.
```bash
# Copy experiment-controller pod's Scalar results
kubectl cp default/experiment-controller:/exp/var ${k8_scalar_dir}/${my_experiment}/scalar-results

# Open the Grafana dashboard in your default browser and take relevant screenshots
open http://$(minikube ip):30345/
```

## (6) Implement an elastic scaling policy that monitors the resource usage__  
This step requires some custom development in the Riemann component. Extend Riemann's configuration with a custom scaling strategy. We recommend checking out http://riemann.io/ to get familiar with the way that events are processed. While Riemann has a slight learning curve, the configuration has access to a Clojure, which is a complete programming language. While out of scope for the provided examplar, new strategies should most often combine events of the same deployment or statefulset by folding them. The image should be build and uploaded to the repository in a similar fashion as demonstrated in step (3).

## (7) Test the elastic scaling policy by executing the workload and measuring the number of SLA violations__  
Finally, this step is very similar to the fifth step. The biggest difference occurs during processing the results. We will use the each request's latency time to determine whether a SLA violation has occured. The implemented scaling policy is ineffective if the service level agreement does not reach its objective.

## (8) Repeat steps 6 and 7 until you have found an elastic scaling policy that works for this workload__  

## Infrastructure
You need to have a Kubernetes cluster, and the kubectl command-line tool must be configured to communicate with your cluster. For example, create a Kubernetes cluster on Amazon Web Services [(tutorial)](https://kubernetes.io/docs/getting-started-guides/aws/) or quickly bootstrap a best-practice cluster using the [kubeadm](https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/) toolkit.

In this tutorial, however, we will use a MiniKube deployment on our local device.
This is just for demonstrating purposes as the resources provided by a single laptop are unsufficient for valid experiment results.
You can, however, follow the same exact steps on a multi-node cluster.
For a more accurate reproduction scenario, we suggest adding labels to each node and add them as constraints to the YAML files of the relevant Kubernetes objects via a [nodeSelector](https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector).

## Operations
__Experiment configuration__  
The experiment has a mandatory configuration to allow communication with the cluster, and an optional configuration to fine-tune experiment parameters. Also, do not forget to use your own repository name in Kubernetes resource declaration files when uploading custom images.

The autoscaler interacts directly with the Kubernetes cluster. The _kubectl_ tool, which is used for this interaction, requires configuration. Secrets are used to pass this sensitive information to the required pods. The next snippet creates the required keys for a MiniKube cluster. First, prepare a directory that contains all the required files. Secondly, change paths to the location at which we will mount the secret (`/root/.kube`). Finally, the last command will create the secret. Do note that the keys required depend on the platform that you have your cluster deployed on.

```bash
mkdir ${k8_scalar_dir}/operations/secrets
cd ${k8_scalar_dir}/operations/secrets
cp ~/.kube/config .
cp ~/.minikube/client.crt .
cp ~/.minikube/client.key .
cp ~/.minikube/ca.crt .

sed -ie "s@/Users/wito/.minikube/@/root/.kube/@g" ./config

kubectl create secret generic kubeconfig --from-file . --namespace=kube-system
```

Several Kubernetes resources can optionally be fine-tuned. Application configuration is done by setting environment variables. For example, the Riemann component can have a strategy configured or the Cassandra cpu threshold at which is should scale. 

Finally, the resource requests and limits of the Cassandra pod can also be adjusted. These files can be found in the `operations` subdirectory, e.g. the Cassandra YAML file can be found in [operations/cassandra-cluster/templates](operations/cassandra-cluster/templates/cassandra-statefulset.yaml).


