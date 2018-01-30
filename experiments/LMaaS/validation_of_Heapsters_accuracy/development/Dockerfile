# Experiment-heapster
FROM ubuntu:16.04 
MAINTAINER Wito Delnat, wito.delnat@student.kuleuven.be

# [Ubuntu] Install kubectl
RUN apt-get update && apt-get install -y curl vim openssh-client
RUN ["/bin/bash", "-c", "curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl"]

# [Ubuntu] Install Cassandra for stress tool
RUN apt-get install -y software-properties-common \
 && add-apt-repository ppa:webupd8team/java \
 && apt-get update \
 && echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections \
 && echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections \
 && apt-get install -y oracle-java8-set-default \
 && echo "deb http://www.apache.org/dist/cassandra/debian 310x main" | tee -a /etc/apt/sources.list.d/cassandra.sources.list \
 && echo "deb-src http://www.apache.org/dist/cassandra/debian 310x main" | tee -a /etc/apt/sources.list.d/cassandra.sources.list \
 && gpg --keyserver pgp.mit.edu --recv-keys F758CE318D77295D \ 
 && gpg --export --armor F758CE318D77295D | apt-key add - \
 && gpg --keyserver pgp.mit.edu --recv-keys 2B5C1B00 \
 && gpg --export --armor 2B5C1B00 | apt-key add - \
 && gpg --keyserver pgp.mit.edu --recv-keys 0353B12C \
 && gpg --export --armor 0353B12C | apt-key add - \
 && apt-get update \
 && apt-get install --allow-unauthenticated -y cassandra

# [Experiment] Initialise
COPY README.md /exp/
COPY bin /exp/bin
COPY etc /exp/etc
WORKDIR /exp

# [SSH] Configuration
RUN mkdir -p ~/.ssh \
 && cp etc/wito-dnet.pem ~/.ssh/wito-dnet.pem

# [Kubectl] Configuration
RUN mkdir -p ~/.kube \
 && cp etc/k8s-cluster-config ~/.kube/config

# Infinite loop to be able to access shell in pod
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
