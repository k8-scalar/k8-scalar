# Usage: "bash uninstall.sh example-experiment", where X is the experiment number 
helm delete $(helm list | grep ${@%/} | awk '{ print $1 }')
