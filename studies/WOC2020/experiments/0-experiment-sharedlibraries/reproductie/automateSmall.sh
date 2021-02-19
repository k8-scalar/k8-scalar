#!/bin/sh
echo "Starting experiment"
echo "dstat: 0 containers"
dstat -m -t -o cont-0.csv 1 10

echo "dstat: 1 container"
docker run -d reproduction
pmap -X $(pidof ./a.out) > pmap1.txt
dstat -m -t -o cont-1.csv 1 10

echo "dstat: 2 containers"
docker rm -f $(docker ps -a -q)
docker run -d reproduction
docker run -d reproduction
pmap -X $(pidof ./a.out) > pmap2.txt
dstat -m -t -o cont-2.csv 1 10

echo "cleanup"
docker rm -f $(docker ps -a -q)

