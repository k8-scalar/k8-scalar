#!/usr/bin/env bash
set -o pipefail
IFS=$'\n\t'

usage() {
echo -n "${BASH_SOURCE[0]} [OPTIONS] [ARGS]

 Performs an experiment to determine the threshhold to start scaling databases.
 
 Parameters:
   userload				Specify user load or, the start user load, end user load and incrementing interval. (Format:  NN or NN:NN:NN)

 Options:
   -d | --duration 		Specify the duration in seconds for each run. (Default: 60)
   -p | --pod 			The initial pod (Default: cassandra-0)
   -h | --help 			Display this message

 Note:
   This script MUST be executed from within the experiment directory.

 Examples:
   # Stress Cassandra with an user load of 125 requests per seconds for 200 seconds
   ${BASH_SOURCE[0]} --duration 200 125

   # Executes the experiment: 10 users for first run, 20 users for second run, .., 100 users for last run.
   ${BASH_SOURCE[0]} 10:100:10

"
exit 0;
}

## CONSTANTS AND VARIABLE DEFAULTS
ARG_REGEX=^[0-9]*\:[0-9]*\:-*[0-9]*$
ARG_SINGLE_RUN_REGEX=^[0-9]*$

duration=60
pod="svc/bsv1"
baseline="false"

request=125
increment=0
limit=125

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
		    duration=${1}
		    ;;
 		-p|--pod)
		    shift;
		    pod="svc/${1}"
 		    ;;
		-i|--incremental)
			baseline="true"
			;;
	  	-h|--help) usage >&2; exit 0 ;;
	    *)
			fatal "Flag provided but not defined: '$1'. Use --help to display usage."
	  esac
	  shift;
	done
	targetMerge=${1}
	shift;
	args=$@

	# VALIDATE INPUT
	if [ -z flag_date ] && ! [[ $flag_date =~ $DATE_REGEX ]] ; then
		info $flag_date
		fatal "Date flag provided but expected date as YYYYMMDD (eg: 20160628)";
	fi

	# Display help as default when no argument is given
	if [ -z $args ] ; then
		usage >&2;
	fi
}
setup_experiment() {
	rm -r /exp/var >&/dev/null
	mkdir /exp/var >&/dev/null
	mkdir -p /exp/var/results /exp/var/logs

	echo "Setting up database mt-api"
	kubectl exec $pod -- bash -c "npm run clear-db && npm run seed-db"
	sleep 2

	echo "Changing deadlines"
	counter=30
	if [ $baseline == "true" ]; then
		echo "Incremental deadlines"
		python bin/replace-deadline.py $counter
	else
		deadTime=$( date -d "+ $counter seconds" +"%T" )
		sed "s/\"deadline\":.*$/\"deadline\": ${deadTime}/g" bin/upgrade-users.json
	fi
	
}
setup_run() {
	local user_peak_load=$1
	local duration=$2
	local target=$3
	local targetMerge=$

# Create temporary files
	cp /exp/etc/experiment-template.properties /tmp/experiment.properties
	sed -ie "s@USER_PEAK_LOAD_TEMPLATE@${user_peak_load}@g" /tmp/experiment.properties
	sed -ie "s@USER_PEAK_DURATION_TEMPLATE@${duration}@g" /tmp/experiment.properties
	sed -ie "s@TARGET_URL_TEMPLATE@${targetMerge}@g" /tmp/experiment.properties
}
teardown_run() {
	local user_peak_load=$1
	
	# Remove temporary files
	rm /tmp/experiment.properties

	# Gather results
	mv results--tmp-experiment-properties.dat /exp/var/results/run-${user_peak_load}.dat
	mv residence-times--tmp-experiment-properties.dat /exp/var/results/residence-times-${user_peak_load}.dat
 	mv gnuplot-capacity.dat /exp/var/results/gnuplot-capacity-${user_peak_load}.dat
	mv gnuplot-heatmap.dat /exp/var/results/gnuplot-heatmap-${user_peak_load}.dat
	mv *.txt /exp/var/logs

	# Remove data added to database
	# kubectl exec $pod -- cqlsh -e "TRUNCATE scalar.logs;"
}
run() {
	local user_peak_load=$1
	local duration=$2
	local target=$3
	local targetMerge=$4

	setup_run $user_peak_load $duration $target $targetMerge
	java -jar /exp/lib/scalar-1.0.0.jar /exp/etc/platform.properties /tmp/experiment.properties >> /exp/var/logs/console-log.txt
	teardown_run $user_peak_load
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

setup_experiment

info "Starting the upgrade from the upgrade-users.json file for incremental upgrades"
curl -X POST -H "Content-Type: application/json" -d @bin/upgrade-users.json http://upgradeplannersvc:8080/tenantconfig

info "Stressing application with ${args} requests per second for ${duration} seconds per run"
run ${args} $duration $pod $targetMerge
