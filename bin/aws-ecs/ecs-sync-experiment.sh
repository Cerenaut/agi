#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

########################################################
# synch code and run folder with ecs instance (via S3)
########################################################

# sync to S3
./s3-sync-up.sh

# syc from S3 to ec2 instance (NOTE: you must setup your ssh keys for different hosts in .ssh/config)
ssh ec2-user@c.x.agi.io 'bash -s' < $AGI_HOME/bin/aws-ecs/s3-sync-down.sh