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
ARG_REGEX=^[0-9]*\:[0-9]*\:[0-9]*$
ARG_SINGLE_RUN_REGEX=^[0-9]*$

duration=60
pod="cassandra-0"

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
		    pod=${1}
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

	# Display help as default when no argument is given
	if [ -z $args ] ; then
		usage >&2;
	fi

	# Normalise args
	if [[ $args =~ $ARG_SINGLE_RUN_REGEX ]] ; then
		args="${args}:${args}:1"
    fi

	if ! [[ $args =~ $ARG_REGEX ]] ; then
		fatal "Unexpected argument format. (Expected xx:yy:zz)"
    fi

    request=$(echo $args | awk -F ":" '{ print $1 }')
    increment=$(echo $args | awk -F ":" '{ print $3 }')
    limit=$(echo $args | awk -F ":" '{ print $2 }')
	return
}
setup_experiment() {
	rm -r /exp/var >&/dev/null
	mkdir /exp/var >&/dev/null
	mkdir -p /exp/var/results /exp/var/logs
	kubectl exec $pod -- cqlsh -e "CREATE KEYSPACE IF NOT EXISTS scalar WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};"
	kubectl exec $pod -- cqlsh -e "CREATE TABLE IF NOT EXISTS scalar.logs (id text PRIMARY KEY, timestamp text, message text);"
}
setup_run() {
	local user_peak_load=$1
	local duration=$2

	# Create temporary files
	cp /exp/etc/experiment-template.properties /tmp/experiment.properties
	sed -ie "s@USER_PEAK_LOAD_TEMPLATE@${user_peak_load}@g" /tmp/experiment.properties
	sed -ie "s@USER_PEAK_DURATION_TEMPLATE@${duration}@g" /tmp/experiment.properties
}
teardown_run() {
	local user_peak_load=$1

	# Remove temporary files
	rm /tmp/experiment.properties

	# Remove unneeded run files.
	#rm -f logfile-*.txt residence-times--tmp-experiment-properties.dat mythreaddump.txt

	# Gather results
	mv results--tmp-experiment-properties.dat /exp/var/results/run-${user_peak_load}.dat
        mv residence-times--tmp-experiment-properties.dat /exp/var/results/residence-times-${user_peak_load}.dat
 	mv gnuplot-capacity.dat /exp/var/results/gnuplot-capacity-${user_peak_load}.dat
        mv gnuplot-heatmap.dat /exp/var/results/gnuplot-heatmap-${user_peak_load}.dat
        mv *.txt /exp/var/logs

	# Remove data added to database
	kubectl exec $pod -- cqlsh -e "TRUNCATE scalar.logs;"
}
run() {
	local user_peak_load=$1
	local duration=$2

	setup_run $user_peak_load $duration
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
for user_load in $(seq $request $increment $limit)
do
	info "Stressing Cassandra with ${user_load} requests per second for ${duration} seconds"
	run $user_load $duration
done
