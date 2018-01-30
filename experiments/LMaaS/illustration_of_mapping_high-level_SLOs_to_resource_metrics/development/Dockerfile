FROM ubuntu:16.04 
MAINTAINER Wito Delnat, wito.delnat@student.kuleuven.be

# [Ubuntu] Install dependencies
RUN apt-get update && apt-get install -y curl vim openssh-client default-jre
RUN ["/bin/bash", "-c", "curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl"]

# [Demo] Initialise
COPY README.md /exp/
COPY bin /exp/bin
COPY etc /exp/etc
COPY lib /exp/lib
WORKDIR /exp

# [SSH] Configuration
RUN mkdir -p ~/.ssh \
 && cp etc/wito-dnet.pem ~/.ssh/wito-dnet.pem

# [Kubectl] Configuration
RUN mkdir -p ~/.kube \
 && cp etc/k8s-cluster.conf ~/.kube/config

# Infinite loop to be able to access shell in pod
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
