FROM ubuntu:16.04 
MAINTAINER Wito Delnat, wito.delnat@student.kuleuven.be

# [Ubuntu] Install dependencies
RUN apt-get update && apt-get install -y curl vim openssh-client default-jre gnuplot
RUN mkdir /opt/local
RUN mkdir /opt/local/bin
RUN ln -s /usr/bin/gnuplot /opt/local/bin/gnuplot
RUN ["/bin/bash", "-c", "curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl"]

# [Demo] Initialise
COPY README.md /exp/
COPY bin /exp/bin
COPY etc /exp/etc
COPY lib /exp/lib
WORKDIR /exp

# Infinite loop to be able to access shell in pod
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
