#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

# synch code and run folder with ecs instance (via S3)
./aws-s3-sync-up.sh

ec2dns="ec2-52-63-253-116.ap-southeast-2.compute.amazonaws.com";
ssh -i "$AGI_HOME/../credentials/nextpair.pem" ec2-user@$ec2dns 'bash -s' < $AGI_HOME/bin/aws-ecs/aws-s3-sync-down.sh