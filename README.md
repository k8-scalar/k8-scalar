# K8-Scalar
For this K8-Scalar 101, we will go over the steps to implement and evaluate elastic scaling policies in container-orchestrated database clusters using the Advanced Riemann-Based Autoscaler (ARBA). Furthermore, additional details about infrastructure and operation are appended. The goal is to enable modification of the K8-Scalar examplar to experiment with other types on autoscalers, for other types of database cluster technologies and other types of workloads.

## Implementing and evaluating elastic scaling policies for container-orchestrated database clusters
This tutorial provides more practical know-how for the related paper. Eight steps allow us to effectively implement and evaluate elastic scaling strategies for specific workloads. 

```bash
# Note1: This tutorial contains many code snippets. They are only tested on MacOS but most should work on Linux.
# Note2: The snippets contain environment variables which should be self-declarative. Do not forget to specify them:
# - ${k8-scalar_dir} = the local directory on your system in which the k8-scalar GitHub project has been cloned
# - ${MyRepository} = the name of the Docker repository of the customized experiment-controller image (based on Scalar). Create in 
#                     advance an account for the ${MyRepository} repository at https://hub.docker.com/                      
```

__(1) Setup a Kubernetes cluster with Heapster activated__  
The setup of a Kubernetes cluster depends on the underlying platform. The _infrastructure_ section provides some references to get started. If you just want to try out the tutorial on MacOS, then you can install MiniKube using the following steps:
```bash
# Install VirtualBox
brew cask install virtualbox
# Install kubectl
curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.9.0/bin/darwin/amd64/kubectl && chmod +x ./kubectl && sudo mv ./kubectl /usr/local/bin/kubectl
# Install MiniKube
curl -Lo minikube https://storage.googleapis.com/minikube/releases/v0.24.1/minikube-darwin-amd64 && chmod +x minikube && sudo mv minikube /usr/local/bin/
# Start MiniKube
minikube start
```

Afterwards, we want to add the monitoring capabilities to the cluster. To ease to installing of the monitoring layer we use [Helm](https://github.com/kubernetes/helm). Helm is a tool for managing Kubernetes charts. Charts are packages of pre-configured Kubernetes objects. We install the _monitoring-core_ chart by the following command. This chart includes instantiated templates for the following Kubernetes services: Heapster, Grafana and the InfluxDb. 
```bash
helm install ${k8-scalar_dir}/operations/monitoring-core
```

__(2) Setup a Database Cluster__  
This _cassandra-cluster_ chart uses a modified image which resolves a missing dependency in one of Google Cassandra's image. Of course, this chart can be replaced with a different database technology. Do mind that Scalar will have to be modified for the experiment with implementations of desired workload generators for the Cassandra database. The next step will provide more information about this modification.
```bash 
helm install ${k8-scalar_dir}/operations/cassandra-cluster
```

__(3) Determine and implement desired workload type for the deployed database in Scalar__  
This step requires some custom development for different database technologies. Extend Scalar with custom _users_ for your database which can read, write or perform more complex operations. For more information how to implement this, we refer to the [Cassandra User classes](development/scalar/src/be/kuleuven/distrinet/scalar/users) and the [Cassandra Request classes](development/scalar/src/be/kuleuven/distrinet/scalar/requests). Afterwards we want to build the application and copy the resulting jar:
```bash
# Extend User with operations for your database in the directory below
cd ${k8-scalar_dir}/development/scalar/src/be/kuleuven/distrinet/scalar/users
vim ${myDatabase}User.java # Cfr CassandraWriteUser.java

# After building the project, copy the resulting Jar file to the experiment-controller image
mv ${k8-scalar_dir}/development/scalar/target/scalar-1-0-0.jar ${k8-scalar_dir}/development/example-experiment/lib/scalar-1-0-0.jar

# Configure the experiment-controller's workload
cd ${k8-scalar_dir}/development/example-experiment/etc/
vim experiment-template.properties # Configure user_implementations, do not modify user_implementations_prestart
``` 

Finally, build a new image for the experiment-controller
```bash
docker build -t ${myRepository}/experiment-controller ${k8-scalar_dir}/development/example-experiment/
# overwrite in following command MyRepository_DOCKERHUB_PASSWRD with your secret password: 
# docker login -u ${myRepository} -p MyRepository_DOCKERHUB_PASSWRD  
docker push ${myRepository}/experiment-controller
```

Scalar is a fully distributed, extensible load testing tool with a numerous features. I recommend checking out https://distrinet.cs.kuleuven.be/software/scalar/ for more information.

__(4) Deploying ARBA__  
Before deploying, check out the [Operations section](README.md#operations) below in this document for performing the necessary Kubernetes secret management and resource configuration. The secret management is mandatory as ARBA requires this to communicate with the Master API of the Kubernetes cluster.
```bash
helm install ${k8-scalar_dir}/operations/arba-with-experiment-controller
```

__(5) Perform experiment for determining the mapping between SLA violations and relevant metrics for the specific workload and database based on asssumptions what are relevant metrics (latency, cpu utilization, disk usage) based on a proven theory (e.g the universal law of scalability or the the law of little).__  
Before starting the experiment, we recommend using the `kubectl get pods --all-namespaces` command to validate that no error occured during the deployment. Finally, we can start the experiment by executing the following command:
```bash
kubectl exec experiment-controller -- bash bin/stress.sh --duration 400 500:1500:1000
```

This command will tell Scalar to gradually increase the workload on the database cluster. The workload is executed as a series of runs. The duration of a single run is set at 400 seconds. The workload starts at a run of 500 requests per second and increases up to 1500 with an increment of 1000 requests per second. For these arguments, the experiment will consist thus of 2 runs and last 800 seconds. Afterwards, experiment results include Scalar statistics and Grafana graphs. The Scalar results are found in the pod experiment-controller pod in the `/exp/var` directory. The Kubernetes cluster exposes a Grafana dashboard at port 30345. Some default graphs are provided, but you can also write your own queries. This snipper provides an easy way to copy the results to the local developer machine. Ofcourse, the second command is only valid when trying out the flow on MiniKube. For realistic clusters, you should determine the IP of any Kubernetes node.
```bash
# Copy experiment-controller pod's Scalar results
kubectl cp default/experiment-controller:/exp/var ${k8-scalar_dir}/experiment/scalar-results

# Open the Grafana dashboard in your default browser and take relevant screenshots
open http://$(minikube ip):30345/
```

__(6) Implement an elastic scaling policy that monitors the resource usage__  
This step requires some custom development in the Riemann component. Extend Riemann's configuration with a custom scaling strategy. We recommend checking out http://riemann.io/ to get familiar with the way that events are processed. While Riemann has a slight learning curve, the configuration has access to a Clojure, which is a complete programming language. While out of scope for the provided examplar, new strategies should most often combine events of the same deployment or statefulset by folding them. The image should be build and uploaded to the repository in a similar fashion as demonstrated in step (3).

__(7) Test the elastic scaling policy by executing the workload and measuring the number of SLA violations__  
Finally, this step is very similar to the fifth step. The biggest difference occurs during processing the results. We will use the each request's latency time to determine whether a SLA violation has occured. The implemented scaling policy is ineffective if the service level agreement does not reach its objective.

__(8) Repeat steps 6 and 7 until you have found an elastic scaling policy that works for this workload__  

## Infrastructure
You need to have a Kubernetes cluster, and the kubectl command-line tool must be configured to communicate with your cluster. For example, create a Kubernetes cluster on Amazon Web Services [(tutorial)](https://kubernetes.io/docs/getting-started-guides/aws/) or quickly bootstrap a best-practise cluster using the [kubeadm](https://kubernetes.io/docs/setup/independent/create-cluster-kubeadm/) toolkit.

In this tutorial, however, we will use a MiniKube deployment on our local device.
This is just for demonstrating purposes as the resources provided by a single laptop are unsufficient for valid experiment results.
You can, however, follow the same exact steps on a multi-node cluster.
For a more accurate reproduction scenario, we suggest adding nodeSelectors to each node and add them as constraints to the YAML files of the relevant Kubernetes objects.

## Operations
__Experiment configuration__  
The experiment has a mandatory configuration to allow communication with the cluster, and an optional configuration to fine-tune experiment parameters. Also, do not forget to use your own repository name in Kubernetes resource declaration files when uploading custom images.

The autoscaler interacts directly with the Kubernetes cluster. The _kubectl_ tool, which is used for this interaction, requires configuration. Secrets are used to pass this sensitive information to the required pods. The next snippet creates the required keys for a MiniKube cluster. First, prepare a directory that contains all the required files. Secondly, change paths to the location at which we will mount the secret (`/root/.kube`). Finally, the last command will create the secret. Do note that the keys required depend on the platform that you have your cluster deployed on.

```bash
mkdir ${k8-scalar_dir}/operations/secrets
cd ${k8-scalar_dir}/operations/secrets
cp ~/.kube/config .
cp ~/.minikube/client.crt .
cp ~/.minikube/client.key .
cp ~/.minikube/ca.crt .

sed -ie "s@/Users/wito/.minikube/@/root/.kube/@g" ./config

kubectl create secret generic kubeconfig --from-file .
```

Several Kubernetes resources can optionally be fine-tuned. Application configuration is done by setting environment variables. For example, the Riemann component can have a strategy configured or the Cassandra cpu threshold at which is should scale. Finally, the resource requests and limits of the Cassandra pod can also be adjusted. The files can have to be modified can be found in `${arba_dir}/operations/example-experiment/templates`.

__Experiment deployment with Helm__  
[Helm](https://github.com/kubernetes/helm) is utilised to deploy the distributed system of the experiment. Helm is a package manager for Kubernetes charts. These charts are packages of pre-configured Kubernetes resources. For this system, we provide three charts. A shared _monitoring-core_ is used across several experiments. This core contains _Heapster_, _Grafana_ and _InfluxDb_. The second chart provides a database cluster and the third the ARBA system with an experiment controller included.
