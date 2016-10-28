#!/bin/bash

# synch the code and run folders with the appropriate S3 bucket

default="$(dirname $0)/../variables.sh"
variables_file=${VARIABLES_FILE:-$default}
echo "Using variables file = $variables_file" 
source $variables_file

aws s3 sync $AGI_HOME 		s3://agief-project/agi --exclude ".git/*"
aws s3 sync $AGI_RUN_HOME 	s3://agief-project/run --exclude ".git/*"