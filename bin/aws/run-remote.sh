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


# WARNING: hardcoded path on remote machine in shell commands below (to be run on remote host via ssh)

ssh -v -i $keyfile ec2-user@${host} -o 'StrictHostKeyChecking no' 'bash -s' <<'ENDSSH' 
	export VARIABLES_FILE="/home/ec2-user/agief-project/variables/variables-ec2.sh"
	source $VARIABLES_FILE
	cd $AGI_HOME/bin/node_coordinator
	./run-in-docker.sh -d
ENDSSH

status=$?

if [ $status -ne 0 ]
then
	echo "ERROR: Could not complete execute run-in-docker.sh on remote machine through ssh." >&2
	echo "	Error status = $status" >&2
	echo "	Exiting now." >&2
	exit $status
fi


exit
ssh -i ~/.ssh/nextpair.pem ec2-user@52.63.242.158 "bash -c \"export VARIABLES_FILE=\"variables-ec2.sh\" && cd /home/ec2-user/agief-project/agi/bin/node_coordinator && ./run-in-docker.sh -d\""