#!/bin/bash

variables_file=${VARIABLES_FILE:-"variables.sh"}
echo "Using variables file = \"$variables_file\""
source $(dirname $0)/../$variables_file

########################################################
# synch code and run folder with ecs instance (via S3)
########################################################

# sync to S3
./aws-s3-sync-up.sh

# syc from S3 to ec2 instance
ssh -i "$AGI_HOME/../credentials/nextpair.pem" ec2-user@x.agi.io 'bash -s' < $AGI_HOME/bin/aws-ecs/aws-s3-sync-down.sh