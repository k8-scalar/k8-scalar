#!/bin/bash
# $1 is the amount of runs that are required

runs=$1
duration=$2

echo "Starting the run loop"
for i in $(seq 1 1 ${runs})
do
    now1=$(date +"%T")
    echo "${now1}: Starting run ${i}"
    mkdir -p validation-data-group/run-${i}

    now2=$(date +"%T")
    echo "${now2}: Full clean the environment"
    bash exp-fullclean.sh --delete

    now3=$(date +"%T")
    echo "${now3}: Let environment settle for 300 seconds (5min)"
    sleep 300

    now4=$(date +"%T")
    echo "${now4}: Start the scalar run && surge-validator"
    # Duurt 15s langer dan de duration om te starten
    nohup java -jar /home/ubuntu/surge-validator/out/artifacts/surge_validator_jar/surge-validator.jar -d 500
    bash exp-scalar-run.sh 50 ${duration} > experiment.out 2> experiment.err < /dev/null
    sleep 200

    now5=$(date +"%T")
    echo "${now5}: Copying files from controller and validator to correct folders"
    curl -X GET http://172.19.113.25:30123/status > validation-data-group/run-${i}/planner.txt
    echo "Copying the results.dat from the scalar"
    kubectl cp experiment-controller-0:results--exp-etc-normal-incremental-experiment-properties.dat validation-data-group/run-${i}/run-13.dat
    echo "Copying output and error files from the filesystem"
    mv output.csv validation-data-group/run-${i}/output-${i}.csv
    mv scalar.out validation-data-group/run-${i}/scalar.out
    mv scalar.err validation-data-group/run-${i}/scalar.err
    mv experiment.out validation-data-group/run-${i}/experiment.out
    mv experiment.err validation-data-group/run-${i}/experiment.err

    now6=$(date +"%T")
    echo "${now6}: Ending run ${i}"
done
now7=$(date +"%T")
echo "${now7}: Ending run loop"
