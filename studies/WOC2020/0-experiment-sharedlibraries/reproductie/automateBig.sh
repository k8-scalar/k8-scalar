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

for i in `seq 3 20`
do
    echo "dstat: $i containers"
    docker rm -f $(docker ps -a -q)
    for j in `seq 1 $i`
    do
        docker run -d reproduction
    done
    pmap -X $(pidof ./a.out) > pmap$i.txt
    dstat -m -t -o cont-$i.csv 1 10
done

echo "cleanup"
docker rm -f $(docker ps -a -q)

