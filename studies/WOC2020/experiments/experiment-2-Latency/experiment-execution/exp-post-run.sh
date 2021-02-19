#!/bin/bash

# kopieer de resultaten naar de host
baseline="false"
while [[ $1 = -?* ]]; do
	  case $1 in
		-i|--incremental)
			baseline="true"
			;;
	  esac
	  shift
	done


echo "Copy results to host"
if [ $baseline == "true" ]; then
    kubectl cp experiment-controller-0:results--exp-etc-normal-incremental-experiment-properties.dat incremental-runs/run-${1}/run-8.dat
else
    kubectl cp experiment-controller-0:results--exp-etc-normal-incremental-experiment-properties.dat group-runs/run-${1}/run-8.dat
fi

