#!/bin/bash

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file

if [ "$1" == "-h" -o "$1" == "--help" -o "$1" == "" ]; then
  echo "Usage: `basename $0` HOST KEY_FILE (default = ~/.ssh/ecs-key.pem)"
  exit 0
fi

host=$1
keyfile=${2:-$HOME/.ssh/ecs-key.pem}

echo "Using keyfile = " $keyfile
echo "Using host = " $host

########################################################
# sync code and run folder with ecs instance (via S3) - DEPRECATED APPROACH
########################################################
# # sync to S3
# ./s3-sync-up.sh

# # sync from S3 to ec2 instance (NOTE: you must setup your ssh keys for different hosts in .ssh/config)
# ssh ec2-user@c.x.agi.io 'bash -s' < $AGI_HOME/bin/aws-ecs/s3-sync-down.sh


########################################################
# synch code and run folder with ecs instance
########################################################

# code
cmd="rsync -ave 'ssh -i $keyfile -o \"StrictHostKeyChecking no\"' $AGI_HOME/ ec2-user@${host}:~/agief-project/agi --exclude={\"*.git/*\",*/src/*}"
echo $cmd
eval $cmd
status=$?

if [ $status -ne 0 ]
then
	echo "ERROR:  Could not complete rsync operation - failed at 'Code' stage." >&2
	echo "	Error status = $status" >&2
	echo "	Exiting now." >&2
	exit $status
fi

# the specific experiment folder
cmd="rsync -ave 'ssh -i $keyfile -o \"StrictHostKeyChecking no\"' $AGI_EXP_HOME/ ec2-user@${host}:~/agief-project/run --exclude={\"*.git/*\"}"
echo $cmd
eval $cmd
status=$?

if [ $status -ne 0 ]
then
	echo "ERROR:  Could not complete rsync operation - failed at 'Experiment-Definitions' stage. Exiting now." >&2
	echo "	Error status = $status" >&2
	echo "	Exiting now." >&2
	exit $status
fi

# the variables folder (with variables.sh files)
cmd="rsync -ave 'ssh -i $keyfile -o \"StrictHostKeyChecking no\"' $AGI_EXP_HOME/../variables/ ec2-user@${host}:~/agief-project/variables --exclude={\"*.git/*\"}"
echo $cmd
eval $cmd
status=$?

if [ $status -ne 0 ]
then
	echo "ERROR: Could not complete rsync operation - failed at 'Variables' stage. Exiting now." >&2
	echo "	Error status = $status" >&2
	echo "	Exiting now." >&2
	exit $status
fi