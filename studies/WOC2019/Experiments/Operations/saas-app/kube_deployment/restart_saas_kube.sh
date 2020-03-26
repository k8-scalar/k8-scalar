#! /bin/bash
kubectl delete deployment,service saas
kubectl create -f deploy_saas_kube.yaml
kubectl create -f expose.yaml 
