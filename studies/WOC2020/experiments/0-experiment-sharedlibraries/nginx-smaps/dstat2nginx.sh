#!/bin/sh
echo "Starting experiment"
echo "dstat: 0 containers"
dstat -m -t -o NGINX-0.csv 1 10

echo "dstat: 1 container"
docker run -d nginx
dstat -m -t -o NGINX-1.csv 1 10

echo "dstat: 2 containers"
docker rm -f $(docker ps -a -q)
docker run -d nginx
docker run -d nginx
dstat -m -t -o NGINX-2.csv 1 10

echo "cleanup"
docker rm -f $(docker ps -a -q)

