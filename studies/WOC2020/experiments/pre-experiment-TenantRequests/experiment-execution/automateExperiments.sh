#!/bin/bash
maxLimit=1600
minLimit=1000
differenceLimit=$(($maxLimit-$minLimit))
hopLimit=100
amount=$(($differenceLimit/$hopLimit))

runtime=300

mkdir redoresults0505
cd redoresults0505

echo "Starting runs from $maxLimit to $minLimit every $hopLimit"
for limit in $(seq $maxLimit -$hopLimit $minLimit)
do
    limitm=${limit}m
    echo "Testing with limit: $limitm"
    kubectl scale -n default deployment mt-api --replicas=0
    sleep 60
    echo "Setting limit."
    kubectl get deploy mt-api -o yaml > temp-mt.yaml && sed -i "s/cpu:.*$/cpu: $limitm/g" temp-mt.yaml && kubectl replace -f temp-mt.yaml
    echo "Scaling up."
    kubectl scale -n default deployment mt-api --replicas=1
    sleep 60
    mkdir results-$limit
    cd results-$limit
    case $limit in
        1600 | 1500 | 1400 | 1300)
          kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 300 1:30:1
          kubectl cp experiment-controller-0:var/results/ .
          ;;
        
        1200 | 1100 | 1000 | 900 | 800 | 700 | 600)
          kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 300 1:25:1
          kubectl cp experiment-controller-0:var/results/ .
          ;;

        500 | 400)
          kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 300 1:10:1
          kubectl cp experiment-controller-0:var/results/ .
          ;;
  
        *)
          kubectl exec experiment-controller-0 -- bash bin/stress.sh --duration 300 1:5:1
          kubectl cp experiment-controller-0:var/results/ .
          ;;
    esac
    cd ..
done
