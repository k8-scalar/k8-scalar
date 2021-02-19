#!/bin/bash

# delete all
if [ $1 == --delete ]; then
    echo "start deleting all the previous deployments"
    kubectl delete -f upgradeplanner/upgradedeployment.yml
    kubectl delete deployment mongo-db
    kubectl delete deployment mt-api-v1
    helm delete exp-cont-0 api-v2 --purge
    sleep 20
else
    echo "start creating the deployments"
fi
    kubectl create -f upgradeplanner/upgradedeployment.yml
    helm install --name exp-cont-0 experiment-controller-0/exp-cont-0/
    kubectl create -f mt-api-v1/templates/db-s.yaml
    kubectl create -f mt-api-v1/templates/db.yaml
    sleep 20
    kubectl create -f mt-api-v1/templates/mt-api-s.yaml
    kubectl create -f mt-api-v1/templates/mt-api.yaml
    helm install --name api-v2 mt-api-v2
