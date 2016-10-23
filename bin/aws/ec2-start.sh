#!/bin/bash

if [ "$1" == "-h" -o "$1" == "--help" ]; then
  echo "Usage: `basename $0` InstanceId X(1=START/0=STOP)"
  echo "All arguments are optional."
  exit 0
fi

# parameters
start=${1:-1}
instanceId=${2:-i-cf55394d}			# node=i-cf55394d, postgres=i-b1d1bd33


if [ "$start" == 1 ]; then
	aws ec2 start-instances --instance-ids $instanceId
else
	aws ec2 stop-instances --instance-ids $instanceId
fi