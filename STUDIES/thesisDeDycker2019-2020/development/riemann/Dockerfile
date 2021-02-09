FROM ubuntu:16.04
MAINTAINER wito

ENV TZ="Europe/Brussels"
RUN apt-get update && apt-get install -y default-jre

RUN mkdir app
WORKDIR app

COPY bin /app/bin
COPY lib /app/lib
COPY etc /app/etc

EXPOSE 5555/tcp 5555/udp 5556
CMD java -cp lib/riemann.jar:lib/clj-http-3.5.0.jar:etc/ riemann.bin start etc/riemann.config
