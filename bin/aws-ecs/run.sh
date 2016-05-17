#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file



# start instances
./ec2-start.sh 1 i-cf55394d		# container instance
./ec2-start.sh 1 i-b1d1bd33		# postgres

# sync files
./ec2-sync-experiment.sh		# sync local files to the container instance

# run task
