tail -n +8 /exp/var/results/run-test.csv | awk -F "," '{print $1 "," 100-$4}'
