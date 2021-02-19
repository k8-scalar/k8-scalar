#!/bin/bash
# $1 = duration that is set per run at a certain limit


# e.g. --> .\experimentSurge 60 8,8,7,6,6,5,4,4,3,2,2,1,1,0 bsv1 bsv2
# this will start 2 controllers that run stress.h --duration 300 8,8,7,6,6,5,4,4,3,2,2,1,1,0 bsv1 and --duration 300 0,1,1,2,2,3,4,4,5,6,6,7,8,8 bsv2

# update: k8-scalar zal normaal dynamisch tenants overplaatsen, dus vorm moet zijn 8,8,8,8,8,8,8,8 want dezelfde controller blijft het sturen. 

url=172.19.112.28:30123
now1=$(date +"%T")
echo "${now1} **-- Starting Experiment 1: Usage, Surge with AVG workload"
baseline="false"
while [[ $1 = -?* ]]; do
	  case $1 in
		-i|--incremental)
			baseline="true"
			;;
	  esac
	  shift
	done

deadLineDuration=$1
runDuration=$2

# seed DB
echo "Seeding the UpgradeplannerDB with tenants"
curl -X POST -H "Content-type: application/json" -d @users-seed.json http://${url}/tenants

# load in deployments
nowSeed1=$(date +"%T")
echo "${nowSeed1} -- Seeding the UpgradeplannerDB with Deployments"
curl -X POST http://${url}/deployment?deployment-name=mt-api-v1
curl -X POST http://${url}/deployment?deployment-name=mt-api-v2

nowSeed2=$(date +"%T")
echo "${nowSeed2} -- Seeding the application database"
kubectl exec $(kubectl get pods --no-headers -o custom-columns=":metadata.name" -l app=mt-api,version=1) npm run clear-db
kubectl exec $(kubectl get pods --no-headers -o custom-columns=":metadata.name" -l app=mt-api,version=1) npm run seed-db


# Setting up the experiment files
nowChange=$(date +"%T")
echo "${nowChange} -- changing experiment files"
counter=${deadLineDuration}
if [ $baseline == "true" ]; then
    echo "Incremental stress testing"
	kubectl cp normal-incremental-experiment.properties experiment-controller-0:/exp/etc
	mkdir -p output
	rm output/normal.sh
	touch output/normal.sh
	chmod +x output/normal.sh
	echo "java -jar /exp/lib/scalar-1.0.0.jar /exp/etc/platform.properties /exp/etc/normal-incremental-experiment.properties" >> output/normal.sh

	# Setting up deadlines
	python replace-deadline.py $counter
else
	echo "Group stress testing"
	kubectl cp normal-group-experiment.properties experiment-controller-0:/exp/etc
	mkdir -p output
	rm output/normal.sh
	touch output/normal.sh
	chmod +x output/normal.sh
	echo "java -jar /exp/lib/scalar-1.0.0.jar /exp/etc/platform.properties /exp/etc/normal-group-experiment.properties" >> output/normal.sh

	# Setting up deadlines
	deadTime=$( date -d "+ ${counter} seconds" +"%T" )
	sed "s/\"deadline\":.*$/\"deadline\": \"${deadTime}\"/g" upgrade-users.json > new-upgrade-users.json
fi
kubectl cp output/normal.sh experiment-controller-0:/exp/

touch output/tmp-run.sh
chmod +x output/tmp-run.sh
sleep 2

now2=$(date +"%T")
echo "${now2} -- Starting scalar script"
echo "kubectl exec experiment-controller-0 -- bash normal.sh" > output/tmp-run.sh 
nohup ./output/tmp-run.sh > scalar.out 2> scalar.err < /dev/null &

now3=$(date +"%T")
echo "${now3} -- Starting upgrade"
curl -X POST -H "Content-Type: application/json" -d @new-upgrade-users.json http://${url}/tenantconfig

sleep $runDuration
now4=$(date +"%T")
echo "${now4} -- Experiment script is over"

