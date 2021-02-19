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
    mkdir -p incremental-runs/run-${i}

    now2=$(date +"%T")
    echo "${now2}: Full clean the environment"
    bash exp-fullclean.sh --delete

    now3=$(date +"%T")
    echo "${now3}: Let environment settle for 420 seconds (7min)"
    sleep 420

    now4=$(date +"%T")
    echo "${now4}: Start the scalar run && surge-validator"
    # Duurt 15s langer dan de duration om te starten
    bash exp-scalar-run.sh --incremental 130 ${duration} > experiment.out 2> experiment.err < /dev/null
    sleep 30

    now5=$(date +"%T")
    echo "${now5}: Copying files from controller and validator to correct folders"
    curl -X GET http://${url}/status > incremental-runs/run-${i}/planner.txt
    echo "Copying the results.dat from the scalar"
    kubectl cp experiment-controller-0:results--exp-etc-normal-incremental-experiment-properties.dat incremental-runs/run-${i}/run-8.dat
    echo "Copying output and error files from the filesystem"
    mv scalar.out incremental-runs/run-${i}/scalar.out
    mv scalar.err incremental-runs/run-${i}/scalar.err
    mv experiment.out incremental-runs/run-${i}/experiment.out
    mv experiment.err incremental-runs/run-${i}/experiment.err

    now6=$(date +"%T")
    echo "${now6}: Ending run ${i}"
done
now7=$(date +"%T")
echo "${now7}: Ending run loop"
