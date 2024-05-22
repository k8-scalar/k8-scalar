FROM ubuntu:16.04

# [Ubuntu] Install dependencies
RUN apt-get update && apt-get install -y curl vim openssh-client default-jre gnuplot
RUN mkdir /opt/local
RUN mkdir /opt/local/bin
RUN ln -s /usr/bin/gnuplot /opt/local/bin/gnuplot
RUN ["/bin/bash", "-c", "curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl && chmod +x ./kubectl && mv ./kubectl /usr/local/bin/kubectl"]
RUN apt-get update && apt-get install build-essential checkinstall -y &&  apt-get install libreadline-gplv2-dev libncursesw5-dev libssl-dev libsqlite3-dev tk-dev libgdbm-dev libc6-dev libbz2-dev -y

# Install Python
RUN apt-get install python -y

# [Demo] Initialise
COPY README.md /exp/
COPY bin /exp/bin
COPY etc /exp/etc
COPY lib /exp/lib
WORKDIR /exp

# Infinite loop to be able to access shell in pod
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
