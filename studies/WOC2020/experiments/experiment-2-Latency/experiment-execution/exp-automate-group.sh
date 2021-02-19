#!/bin/bash
# $1 is the amount of runs that are required

runs=$1
duration=$2
url=172.19.112.28:30123
echo "Starting the run loop"
for i in $(seq 1 1 ${runs})
do
    now1=$(date +"%T")
    echo "${now1}: Starting run ${i}"
    mkdir -p group-runs/run-${i}

    now2=$(date +"%T")
    echo "${now2}: Full clean the environment"
    bash exp-fullclean.sh --delete

    now3=$(date +"%T")
    echo "${now3}: Let environment settle for 420seconds"
    sleep 420

    now4=$(date +"%T")
    echo "${now4}: Start the scalar run && surge-validator"
    # Duurt 15s langer dan de duration om te starten
    bash exp-scalar-run.sh 20 ${duration} > experiment.out 2> experiment.err < /dev/null
    sleep 60

    now5=$(date +"%T")
    echo "${now5}: Copying files from controller and validator to correct folders"
    curl -X GET http://${url}/status > group-runs/run-${i}/planner.txt
    echo "Copying the results.dat from the scalar"
    kubectl cp experiment-controller-0:results--exp-etc-normal-group-experiment-properties.dat group-runs/run-${i}/run-13.dat
    echo "Copying output and error files from the filesystem"
    mv scalar.out group-runs/run-${i}/scalar.out
    mv scalar.err group-runs/run-${i}/scalar.err
    mv experiment.out group-runs/run-${i}/experiment.out
    mv experiment.err group-runs/run-${i}/experiment.err

    now6=$(date +"%T")
    echo "${now6}: Ending run ${i}"
done
now7=$(date +"%T")
echo "${now7}: Ending run loop"
