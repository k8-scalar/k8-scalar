#!/usr/bin/env bash
set -eo pipefail
IFS=$'\n\t'
EXPERIMENT="experiment-2"

## HELPER FUNCTIONS
info()    { echo "[INFO]    $@"; }

## MAIN
if [[ "${BASH_SOURCE[0]}" != "$0" ]]; then
	fatal "Script may not be sourced."
fi

info "Installing deployment.."
helm install ${EXPERIMENT} &> /dev/null
sleep 30

info "Current number of Cassandra instances:"
echo $(kubectl describe statefulset cassandra | grep Replicas | awk '{ print $2 }')

info "Executing experiment.."
kubectl exec experiment-controller -- bash bin/stress.sh --duration 400 500:1500:1000

info "New number of Cassandra instances:"
echo $(kubectl describe statefulset cassandra | grep Replicas | awk '{ print $2 }')

# info "Uninstalling deployment.."
# helm delete $(helm list | grep experiment | awk '{ print $1 }')

echo "-------------------------------------------------------------------"
echo ""
