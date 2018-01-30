#!/usr/bin/env bash
set -o pipefail
IFS=$'\n\t'

usage() {
echo -n "${BASH_SOURCE[0]} [OPTIONS] [ARGS]

 Performs an experiment to validate the correctness of heapster.
 
 Parameters:
   loop					Specify the start user load, end user load and incrementing interval. (Format: NN:NN:NN)

 Options:
   -d | --duration 		Specify the duration in seconds for each run. (Default: 60)
   -h | --help 			Display this message

"
exit 0;
}

## CONFIGURE VARIABLES
KUBE_WORKER_IP="172.19.24.140"

## HELPER FUNCTIONS
readonly LOG_FILE="/exp/var/logs/$(basename "$0").log"
info()    { echo "[INFO]    $@" | tee -a "$LOG_FILE" >&2 ; }
warning() { echo "[WARNING] $@" | tee -a "$LOG_FILE" >&2 ; }
error()   { echo "[ERROR]   $@" | tee -a "$LOG_FILE" >&2 ; }
fatal()   { echo "[FATAL]   $@" | tee -a "$LOG_FILE" >&2 ; exit 1 ; }
cleanup() {
	return
}
get_input() {
	# PARSE INPUT
	while [[ $1 = -?* ]]; do
	  case $1 in
		-d|--duration)
		    shift;
		    flag_duration=${1}
		    ;;
	  	-h|--help) usage >&2; exit 0 ;;
	    *)
			fatal "Flag provided but not defined: '$1'. Use --help to display usage."
	  esac
	  shift
	done
	args=$@

	# VALIDATE INPUT
	if [ -z flag_date ] && ! [[ $flag_date =~ $DATE_REGEX ]] ; then
		info $flag_date
		fatal "Date flag provided but expected date as YYYYMMDD (eg: 20160628)";
	fi

	# # Display help as default when no argument is given
	# if [ -z $args ] ; then
	# 	usage >&2;
	# fi
	return
}
setup_experiment() {
	mkdir -p /exp/var/logs
	mkdir -p /exp/var/results
	ssh-add ~/.ssh/wito-dnet.pem
}
setup_run() {
	# Start dstat
	ssh ubuntu@${KUBE_WORKER_IP} 'dstat --time --cpu --output run.csv --noupdate 2 > run.txt &'
}
teardown_run() {
	local run_id="test"
	# Stop dstat
    ssh ubuntu@${KUBE_WORKER_IP} 'ps aux | grep /usr/bin/dstat | grep -v grep | awk "{print $2}" | xargs kill'

	# Collect dstat data
	scp ubuntu@${KUBE_WORKER_IP}:~/run.csv /exp/var/results/run-${run_id}.csv
	ssh ubuntu@${KUBE_WORKER_IP} 'rm run.csv run.txt'

	# Collect heapster data
	return
}
run() {
	setup_run

	NODES="cassandra-0.cassandra.default.svc.cluster.local,cassandra-1.cassandra.default.svc.cluster.local"
	cassandra-stress write duration=1m -node $NODES

	teardown_run
}
teardown_experiment() {
	return
}

## MAIN
if [[ "${BASH_SOURCE[0]}" != "$0" ]]; then
	fatal "Script may not be sourced."
fi
trap cleanup EXIT
get_input $@

info "Started experiment"
setup_experiment
run
info "Finished experiment"

